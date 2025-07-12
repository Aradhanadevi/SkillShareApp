package anjali.learning.skilshare.Adapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

import anjali.learning.skilshare.CourseDetailFragment;
import anjali.learning.skilshare.R;
import anjali.learning.skilshare.model.Course;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    private List<Course> courseList;

    public CourseAdapter(List<Course> courseList) {
        this.courseList = courseList;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = courseList.get(position);
        holder.courseName.setText(course.getCourseName());
        holder.price.setText("🎬 " + course.getNoofvideos() + " videos");

        // 🔥 Show "Top Pick" if more than 50 videos
        if (course.getNoofvideos() > 50) {
            holder.topPickBadge.setVisibility(View.VISIBLE);
        } else {
            holder.topPickBadge.setVisibility(View.GONE);
        }

        Glide.with(holder.itemView.getContext())
                .load(course.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_foreground)
                .into(holder.courseImage);

        //  Add dynamic skill chips
        holder.skillTags.removeAllViews();
        if (course.getSkills() != null) {
            for (String tag : course.getSkills().split(",")) {
                Chip chip = new Chip(holder.itemView.getContext());
                chip.setText(tag.trim());
                chip.setClickable(false);
                chip.setCheckable(false);
                holder.skillTags.addView(chip);
            }
        }

        //  Navigate to detail fragment on click
        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("name", course.getCourseName());
            bundle.putString("tutor", course.getTutor());
            bundle.putString("description", course.getDescription());
            bundle.putString("language", course.getLanguage());
            bundle.putString("location", course.getLocation());
            bundle.putInt("videos", course.getNoofvideos());
            bundle.putString("imageUrl", course.getImageUrl());
            bundle.putString("category", course.getCategory());
            bundle.putString("skils", course.getSkills());
            bundle.putInt("price", course.getPrice());

            CourseDetailFragment detailFragment = new CourseDetailFragment();
            detailFragment.setArguments(bundle);

            ((FragmentActivity) v.getContext()).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    public static class CourseViewHolder extends RecyclerView.ViewHolder {

        TextView courseName, price;
        ImageView courseImage;
        Button registerBtn;
        TextView topPickBadge;
        ChipGroup skillTags;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            courseName = itemView.findViewById(R.id.title);
            price = itemView.findViewById(R.id.count);
            courseImage = itemView.findViewById(R.id.thumbnail);
            registerBtn = itemView.findViewById(R.id.registerBtn); // optional use
            topPickBadge = itemView.findViewById(R.id.topPickBadge);
            skillTags = itemView.findViewById(R.id.skillTags);
        }
    }
}
