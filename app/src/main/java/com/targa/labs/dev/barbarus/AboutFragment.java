package com.targa.labs.dev.barbarus;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.targa.labs.dev.barbarus.databinding.FragmentAboutBinding;

public class AboutFragment extends Fragment {

    private FragmentAboutBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentAboutBinding.inflate(inflater, container, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding.aboutLink.setText(Html.fromHtml(getString(R.string.about_url), Html.FROM_HTML_MODE_COMPACT));
        } else {
            binding.aboutLink.setText(Html.fromHtml(getString(R.string.about_url)));
        }
        Linkify.addLinks(binding.aboutLink, Linkify.ALL);
        binding.aboutLink.setMovementMethod(LinkMovementMethod.getInstance());
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}