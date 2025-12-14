package com.example.login;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.RadioButton;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import com.example.login.databinding.FragmentAddEventBinding;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddEventFragment extends Fragment {
    FragmentAddEventBinding binding;
    Uri selectedFile;
    ActivityResultLauncher<Intent> filePickerLauncher;
    NumberPicker numberPicker;

    String docID;
    private String selectedScope;
    private int recruitNum = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAddEventBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();

        //수정 시 기존 내용 표시
        if (getArguments() != null){
            docID = getArguments().getString("documentId");
            binding.titleEditText.setText(getArguments().getString("title"));
            binding.startDateEditText.setText(getArguments().getString("startDate"));
            binding.endDateEditText.setText(getArguments().getString("endDate"));
            binding.location.setText(getArguments().getString("location"));
            binding.content.setText(getArguments().getString("content"));

            String imageStr = getArguments().getString("imageUri");
            if (imageStr != null){
                selectedFile = Uri.parse(imageStr);
                binding.fileEditText.setText(getFileNameFromUri(selectedFile));
            }
        }

        // 파일 첨부 런처
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedFile = result.getData().getData();
                        binding.fileEditText.setText(getFileNameFromUri(selectedFile));
                    }
                });

        // 일정 선택
        binding.startDateEditText.setOnClickListener(v -> showDateTimePicker(startDate, true));
        binding.endDateEditText.setOnClickListener(v -> showDateTimePicker(endDate, false));

        // 파일 첨부
        binding.fileEditText.setOnClickListener(v -> {
            String[] mimeTypes = {"application/pdf", "image/jpeg", "image/png"};
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*")
                    .putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                    .addCategory(Intent.CATEGORY_OPENABLE);
            filePickerLauncher.launch(intent);
        });


        //종류 선택
        binding.categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();

                if (selected.equals("공지")) removeExtraView();
                else addExtraView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });


        binding.radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton radioButton = view.findViewById(checkedId);
            if (radioButton != null) {
                selectedScope = radioButton.getText().toString();
                Log.d("동아링", "Radio Selected: " +radioButton.getText());
            }
        });

        // 제출 버튼
        binding.submitButton.setOnClickListener(v -> {
            if (selectedFile != null) uploadImgAndSaveEvent();
            else saveEvent(null);
        });
    }

    private void uploadImgAndSaveEvent() {
        FirebaseStorage storage = FirebaseStorage.getInstance();

        ContentResolver resolver = requireContext().getContentResolver();
        String mimeType = resolver.getType(selectedFile);
        String extension = MimeTypeMap.getSingleton()
                .getExtensionFromMimeType(mimeType);

        if (extension == null) {
            extension = "jpg"; // fallback
        }
        StorageReference storageRef = storage.getReference().child("events/" + System.currentTimeMillis() + extension);

        storageRef.putFile(selectedFile)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return storageRef.getDownloadUrl();
                })
                .addOnSuccessListener(downloadUri -> {
                    String imageUrl = downloadUri.toString();
                    saveEvent(imageUrl); // downloadUrl 넘김
                })
                .addOnFailureListener(e -> {
                    Log.d("동아링", "Image upload failed", e);
                });
    }

    /// firestore의 clubs 컬렉션에서 해당 club name의 하위 컬렉션인 events에 문서 저장하도록
    private void saveEvent(@Nullable String imageUrl) {

        String title = binding.titleEditText.getText().toString();
        String start = binding.startDateEditText.getText().toString();
        String end = binding.endDateEditText.getText().toString();
        String location = binding.location.getText().toString();
        String content = binding.content.getText().toString();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String clubId = getArguments().getString("clubId");
        DocumentReference parentDocRef = db.collection("clubs").document(clubId);

        Map<String, Object> event = new HashMap<>();
        event.put("title", title);
        event.put("startDate", start);
        event.put("endDate", end);
        event.put("location", location);
        event.put("content", content);
        event.put("imageUri", imageUrl);
        event.put("visibility", selectedScope);
        if (recruitNum != -1) event.put("recruitNum", recruitNum);


        if (docID != null){
            //수정
            parentDocRef.collection("events")
                    .document(docID)
                    .update(event)
                    .addOnSuccessListener(a -> {
                        Log.d("동아링", "update completed");

                        Bundle result = new Bundle();
                        result.putBoolean("success", true);

                        getParentFragmentManager().setFragmentResult("event_added", result);
                        getParentFragmentManager().popBackStack();
                    });
        } else {
            //신규 등록
            parentDocRef.collection("events")
                    .add(event)
                    .addOnSuccessListener(a -> {
                        Log.d("동아링", "registration completed");

                        Bundle result = new Bundle();
                        result.putBoolean("success", true);

                        getParentFragmentManager().setFragmentResult("event_added", result);
                        getParentFragmentManager().popBackStack();
                    });
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String result = null;

        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = requireActivity()
                    .getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                }
            }
        }

        if (result == null) result = uri.getLastPathSegment();

        return result;
    }

    //종류에 따라 창 띄우기
    //공지일 때
    private void removeExtraView(){
        if (numberPicker != null) {
            binding.extraContainer.removeView(numberPicker);
            numberPicker = null;
            recruitNum = -1;
        }

        binding.extraRadio.setVisibility(View.GONE);
    }

    //모집일 때
    private void addExtraView(){
        if (numberPicker != null) return;

        numberPicker = new NumberPicker(requireContext());
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(100);

        numberPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            recruitNum = newVal;
            Log.d("동아링", "NumberPicker Changed: " + newVal);
        });

        binding.extraContainer.addView(numberPicker);

        binding.extraRadio.setVisibility(View.VISIBLE);
    }

    private void showDateTimePicker(Calendar calendar, boolean isStart) {
        Context context = requireContext();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                context,
                (view, year1, month1, dayOfMonth) -> {
                    // 날짜 선택 후 시간 다이얼로그
                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                            context,
                            (view1, hourOfDay, min) -> {
                                String dateTime = String.format(
                                        "%04d-%02d-%02d %02d:%02d",
                                        year1, month1 + 1, dayOfMonth, hourOfDay, min
                                );
                                if (isStart) binding.startDateEditText.setText(dateTime);
                                else binding.endDateEditText.setText(dateTime);
                            },
                            hour,
                            minute,
                            false
                    );

                    timePickerDialog.show();
                },
                year, month, day
        );
        datePickerDialog.show();
    }
}
