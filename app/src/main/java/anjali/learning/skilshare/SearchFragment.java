package anjali.learning.skilshare;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import anjali.learning.skilshare.Adapter.CourseAdapter;
import anjali.learning.skilshare.model.Course;

public class SearchFragment extends Fragment {

    private RecyclerView recyclerView;
    private CourseAdapter adapter;
    private TextView emptyView;
    private List<Course> courseList;

    public SearchFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        recyclerView = view.findViewById(R.id.searchRecyclerView);
        emptyView = view.findViewById(R.id.emptyView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        courseList = (List<Course>) getArguments().getSerializable("courses");

        if (courseList != null && !courseList.isEmpty()) {
            adapter = new CourseAdapter(courseList);
            recyclerView.setAdapter(adapter);
            emptyView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.VISIBLE);
        }

        return view;
    }
}


