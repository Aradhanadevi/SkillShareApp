package anjali.learning.skilshare.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import anjali.learning.skilshare.R;
import anjali.learning.skilshare.model.Course;

public class RegisteredCourseAdapter extends RecyclerView.Adapter<RegisteredCourseAdapter.ViewHolder> {

    private List<Course> courseList;
    private CourseClickListener listener;

    public interface CourseClickListener {
        void onCourseClick(Course course);
        void onCancelClick(Course course);
    }

    public RegisteredCourseAdapter(List<Course> courseList, CourseClickListener listener) {
        this.courseList = courseList;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView courseImage;
        TextView courseName, tutor, price, progressText;
        Button goToCourseBtn, cancelBtn;
        ProgressBar progressBar;

        public ViewHolder(View itemView) {
            super(itemView);
            courseImage = itemView.findViewById(R.id.courseImage);
            courseName = itemView.findViewById(R.id.courseName);
            tutor = itemView.findViewById(R.id.tutorName);
            price = itemView.findViewById(R.id.coursePrice);
            goToCourseBtn = itemView.findViewById(R.id.goToCourseBtn);
            cancelBtn = itemView.findViewById(R.id.cancelCourseBtn);
            progressBar = itemView.findViewById(R.id.progressBar); // ✅ FIXED ID
            progressText = itemView.findViewById(R.id.progressText);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_registered_course, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Course course = courseList.get(position);

        holder.courseName.setText(course.getCourseName());
        holder.tutor.setText("By " + course.getTutor());
        holder.price.setText(course.getPrice() == 0 ? "Free" : "₹" + course.getPrice());

        Glide.with(holder.itemView.getContext())
                .load(course.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.courseImage);

        // ✅ Show progress
        int prog = course.getProgress();
        holder.progressBar.setProgress(prog);
        holder.progressText.setText(course.getProgress() + "%"); // Just show percentage

        holder.goToCourseBtn.setOnClickListener(v -> listener.onCourseClick(course));
        holder.cancelBtn.setOnClickListener(v -> listener.onCancelClick(course));
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }
}
