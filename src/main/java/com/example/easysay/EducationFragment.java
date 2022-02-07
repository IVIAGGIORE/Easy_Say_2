package com.example.easysay;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EducationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EducationFragment extends Fragment {



    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private EditText tbInputCharEdu;
    private Button btnImagePositionHendEdu;
    private Button btnStartEdu;
    private Button btnStopEdu;
    OnMessageEducation message;

    public  interface  OnMessageEducation
    {
        public  void OnMessageRead1(String message);
        public void НачатьОбучение();
        public void ОкончитьОбучение();
    }

    public EducationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EducationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EducationFragment newInstance(String param1, String param2) {
        EducationFragment fragment = new EducationFragment();
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
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_education, container, false);
        tbInputCharEdu = v.findViewById(R.id.tbInputCharEdu);
        btnImagePositionHendEdu = v.findViewById(R.id.btnImagePositionHendEdu);
        btnImagePositionHendEdu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message1 = tbInputCharEdu.getText().toString();
                message.OnMessageRead1(message1);
            }
        });

        btnStartEdu = v.findViewById(R.id.btnStartEdu);
        btnStartEdu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message.НачатьОбучение();
            }
        });
        btnStopEdu = v.findViewById(R.id.btnStopEdu);
        btnStopEdu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message.ОкончитьОбучение();
            }
        });


        return v;
    }

    public void onAttach(Context context){
        super.onAttach(context);

        Activity activity = (Activity) context;


        try{
            message = (EducationFragment.OnMessageEducation) activity;
        }catch (ClassCastException e){
            throw new ClassCastException(activity.toString()+" mus");
        }


    }

    }



