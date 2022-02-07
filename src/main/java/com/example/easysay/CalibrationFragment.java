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
import android.widget.Spinner;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CalibrationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CalibrationFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private EditText editText;
    private Button btnImagePositionHend;
    private Button btnSavePositionHend;
    private Button btnClearPositionHend;
    OnMessage message;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private EditText nameFragTxt;
    private Spinner launchYearSpinner;
    private Button sendBtn;

    public CalibrationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CalibrationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CalibrationFragment newInstance(String param1, String param2) {


        CalibrationFragment fragment = new CalibrationFragment();
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
    public  interface  OnMessage
    {
        public  void OnMessageRead(String message);
        public  void СохранитьПоложениеРуки(String strIncom);
        public  void ОчиститьКалибровку();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_calibration,container,false);
        editText = v.findViewById(R.id.tbInputCharCalibr);

        btnImagePositionHend = v.findViewById(R.id.btnImagePositionHendCalibr);
        btnImagePositionHend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message1 = editText.getText().toString();

                message.OnMessageRead(message1);


            }
        });

        btnSavePositionHend = v.findViewById(R.id.btnSavePositionHend);
        btnSavePositionHend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText().toString().length() == 1) message.СохранитьПоложениеРуки(editText.getText().toString());
                editText.setText("");
            }
        });

        btnClearPositionHend = v.findViewById(R.id.btnClearPositionHend);
        btnClearPositionHend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message.ОчиститьКалибровку();
            }
        });

        return v;
    }


    public void onAttach(Context context){
        super.onAttach(context);

        Activity activity = (Activity) context;


        try{
            message = (OnMessage) activity;
        }catch (ClassCastException e){
            throw new ClassCastException(activity.toString()+" mus");
        }


    }


}