package edu.ismt.sandeshbhusal.mealmate.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import edu.ismt.sandeshbhusal.mealmate.DailyPlanFragment;

public class WeeklyPlanAdapter extends FragmentStateAdapter {
    private final List<Date> dates = new ArrayList<>();
    private final List<DailyPlanFragment> fragments = new ArrayList<>();

    public WeeklyPlanAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        Calendar calendar = Calendar.getInstance();

        for (int i = 0; i < 7; i++) {
            Date date = calendar.getTime();
            dates.add(date);
            fragments.add(DailyPlanFragment.newInstance(date)); // ✅ Store fragment instances
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments.get(position); // ✅ Returns the correct fragment for each tab
    }

    public void updateDateForPosition(int position, Date date) {
        if (position < dates.size()) {
            dates.set(position, date);

            // ✅ Ensure the fragment exists before calling updateDate()
            if (position < fragments.size() && fragments.get(position) != null) {
               // fragments.get(position).updateDate(date);
            }
        }
    }

    // 🔥 This method allows ViewPager2 to get the correct date for each tab
    public Date getDateForPosition(int position) {
        return dates.get(position);
    }

    @Override
    public int getItemCount() {
        return dates.size(); // 7 Days (Today + Next 6 Days)
    }
}
