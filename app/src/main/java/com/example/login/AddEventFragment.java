package com.example.login;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import com.google.firebase.firestore.FirebaseFirestore;

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
                Log.d("LSJ", "Radio Selected: " +radioButton.getText());
            }
        });

        // 제출 버튼
        binding.submitButton.setOnClickListener(v -> saveEvent());
    }

    /// firestore의 clubs 컬렉션에서 해당 club name의 하위 컬렉션인 events에 문서 저장하도록 코드변경 해야함!!!
    private void saveEvent() {
        String title = binding.titleEditText.getText().toString();
        String start = binding.startDateEditText.getText().toString();
        String end = binding.endDateEditText.getText().toString();
        String location = binding.location.getText().toString();
        String content = binding.content.getText().toString();

        String selectedUriString = (selectedFile != null) ? selectedFile.toString() : null;

        Map<String, Object> map = new HashMap<>();
        map.put("event name", title);
        map.put("start date", start);
        map.put("end date", end);
        map.put("location", location);
        map.put("content", content);
        map.put("imageUri", selectedUriString);
        map.put("disclosure scope", selectedScope);
        if (recruitNum != -1) map.put("recruit num", recruitNum);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (docID != null){
            //수정
            db.collection("events")
                    .document(docID)
                    .update(map)
                    .addOnSuccessListener(a -> {
                        Log.d("LSJ", "update completed");

                        Bundle result = new Bundle();
                        result.putBoolean("success", true);

                        getParentFragmentManager().setFragmentResult("event_added", result);
                        getParentFragmentManager().popBackStack();
                    });
        } else {
            //신규 등록
            db.collection("events")
                    .add(map)
                    .addOnSuccessListener(a -> {
                        Log.d("LSJ", "registration completed");

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
            Log.d("LSJ", "NumberPicker Changed: " + newVal);
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
