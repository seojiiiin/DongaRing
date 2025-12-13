package com.example.login;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.login.databinding.FragmentClubInformBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ClubInformFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ClubInformFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    FragmentClubInformBinding binding;

    public ClubInformFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ClubInformFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ClubInformFragment newInstance(String param1, String param2) {
        ClubInformFragment fragment = new ClubInformFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentClubInformBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View chatRow = view.findViewById(R.id.chatInquiryRow);

        chatRow.setOnClickListener(v -> {
            String clubId = getArguments() != null ? getArguments().getString("clubId") : null;
            if (clubId == null) return;

            ChatRoomFragment f = new ChatRoomFragment();
            Bundle b = new Bundle();
            b.putString("clubId", clubId);
            f.setArguments(b);

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.full_screen_container, f) // 여기 컨테이너 id를 프로젝트에 맞게
                    .addToBackStack(null)
                    .commit();
        });
    }


}