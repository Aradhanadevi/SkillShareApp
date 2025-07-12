package anjali.learning.skilshare.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import anjali.learning.skilshare.R;
import anjali.learning.skilshare.model.Course;

public class FeaturedCourseAdapter extends RecyclerView.Adapter<FeaturedCourseAdapter.FeaturedViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Course course);
    }

    private List<Course> courseList;
    private Context context;
    private OnItemClickListener listener;

    public FeaturedCourseAdapter(List<Course> courseList, Context context, OnItemClickListener listener) {
        this.courseList = courseList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FeaturedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_featured_course, parent, false);
        return new FeaturedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeaturedViewHolder holder, int position) {
        Course course = courseList.get(position);

        holder.txtTitle.setText(course.getCourseName());

        Glide.with(context)
                .load(course.getImageUrl())
                .placeholder(R.drawable.featured_course_image)
                .into(holder.imgThumbnail);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(course);
            }
        });
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    static class FeaturedViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumbnail;
        TextView txtTitle;

        public FeaturedViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumbnail = itemView.findViewById(R.id.imgThumbnail);
            txtTitle = itemView.findViewById(R.id.txtTitle);
        }
    }
}
