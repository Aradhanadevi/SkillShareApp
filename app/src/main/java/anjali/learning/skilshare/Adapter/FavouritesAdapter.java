package anjali.learning.skilshare.Adapter;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import anjali.learning.skilshare.CourseDetailFragment;
import anjali.learning.skilshare.R;
import anjali.learning.skilshare.model.Course;

public class FavouritesAdapter extends RecyclerView.Adapter<FavouritesAdapter.ViewHolder> {

    Context context;
    List<Course> courseList;
    String username;

    public FavouritesAdapter(Context context, List<Course> courseList, String username) {
        this.context = context;
        this.courseList = courseList;
        this.username = username;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_course_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Course course = courseList.get(position);

        holder.name.setText(course.getCourseName());
        holder.tutor.setText(course.getTutor());

        Glide.with(context)
                .load(course.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.image);

        // Start with filled heart icon
        holder.heart.setImageResource(R.drawable.ic_heart_filled);
        holder.btnGoToCourse.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("name", course.getCourseName());
            bundle.putString("tutor", course.getTutor());
            bundle.putString("description", course.getDescription());
            bundle.putString("language", course.getLanguage());
            bundle.putString("location", course.getLocation());
            bundle.putString("imageUrl", course.getImageUrl());
            bundle.putString("category", course.getCategory());
            bundle.putString("skils", course.getSkills());
            bundle.putInt("videos", course.getNoofvideos());
            bundle.putInt("price", course.getPrice());

            CourseDetailFragment courseDetailFragment = new CourseDetailFragment();
            courseDetailFragment.setArguments(bundle);

            ((FragmentActivity) context).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, courseDetailFragment) // container ID from main layout
                    .addToBackStack(null)
                    .commit();
        });



        holder.heart.setOnClickListener(v -> {
            DatabaseReference favRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(username)
                    .child("favourites")
                    .child(course.getCourseName());

            // Remove from Firebase
            favRef.removeValue();

            // Remove from local list and notify RecyclerView
            courseList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, courseList.size());

            Toast.makeText(context, "Removed from favourites", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, tutor;
        ImageView image, heart;
        Button btnGoToCourse;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.cardCourseName);
            tutor = itemView.findViewById(R.id.cardTutor);
            image = itemView.findViewById(R.id.cardImage);
            heart = itemView.findViewById(R.id.cardHeartIcon);
            btnGoToCourse = itemView.findViewById(R.id.btnGoToCourse); // new
        }
    }

}
