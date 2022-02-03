package com.jams.pravart.ui.dashboard;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.jams.pravart.R;
import com.jams.pravart.adpater.report_adpater;
import com.jams.pravart.model.report_model;
import com.squareup.picasso.Picasso;

public class DashboardFragment extends Fragment {


    RecyclerView firestoreRecyclerView;
    FirebaseFirestore firebaseFirestore;
    report_adpater adapter;




    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

       firestoreRecyclerView = root.findViewById(R.id.firestorercview);
       firebaseFirestore = FirebaseFirestore.getInstance();


        Query query = firebaseFirestore.collection("report").orderBy("timestamp");
        //adapter
        FirestoreRecyclerOptions<report_model> option = new FirestoreRecyclerOptions.Builder<report_model>()
                .setQuery(query,report_model.class)
                .build();

        adapter = new report_adpater(option);

        firestoreRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        firestoreRecyclerView.setAdapter(adapter);

        return  root;
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }
    @Override
    public void onStop(){
        super.onStop();
        adapter.startListening();
    }

}