package com.synergy.synergyet.custom;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.synergy.synergyet.R;

public class CustomDialogFragment extends DialogFragment {

    /**
     * El constructor vacío es necesario para el DialogFragment.
     * No se pueden añadir parámetros en este constructor.
     */
    public CustomDialogFragment() {}

    public static CustomDialogFragment newInstance(String msg) {
        CustomDialogFragment frag = new CustomDialogFragment();
        Bundle args = new Bundle();
        args.putString("msg", msg);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomAlertDialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.custom_progress_dialog, container);
        // Ponemos el mensaje en el Dialog
        TextView tv_message = view.findViewById(R.id.loading_msg);
        tv_message.setText(getArguments().getString("msg", view.getContext().getString(R.string.default_progress_dialog)));
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

}
