package com.jams.pravart.adpater;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.jams.pravart.R;
import com.jams.pravart.model.report_model;
import com.jams.pravart.ui.dashboard.DashboardFragment;
import com.squareup.picasso.Picasso;

public class report_adpater extends  FirestoreRecyclerAdapter<report_model,report_adpater.data_holder>  {

    public report_adpater(@NonNull FirestoreRecyclerOptions<report_model> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull data_holder holder, int position, @NonNull report_model model) {

        Log.d("TAG","onBindViewHolder: "+model);
        holder.loc_text.setText(model.getLocation());
        Picasso.get()
                .load(model.getImage())
                .into(holder.acc_image);

    }

    @NonNull
    @Override
    public data_holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.report_card,parent, false);

        return new data_holder(view);

    }

    static class data_holder extends RecyclerView.ViewHolder{

        public TextView loc_text;
        public ImageView acc_image;

        public  data_holder(View item){
            super( item);

            loc_text = item.findViewById(R.id.acc_loc);
            acc_image = itemView.findViewById(R.id.acc_img);

        }

    }

 }
