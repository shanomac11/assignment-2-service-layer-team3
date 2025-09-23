package edu.trincoll.repository;

import java.time.LocalDate;
import edu.trincoll.model.Habit;
import java.util.List;

/**
 * Repository interface for managing Habit entities.
 * Provides domain-specific query methods for habit tracking.
 *
 * AI Report:
 * - This interface supports querying habits by archived status, frequency, name, streaks, completion dates, overdue status, and creation date.
 * - Enables flexible retrieval of habits for analytics and user feedback.
 */
public interface HabitRepository extends Repository<Habit, Long> {

    /**
     * Finds all habits with the specified archived status.
     * @param archived true to find archived habits, false otherwise
     * @return list of matching habits
     */
    List<Habit> findByArchived(boolean archived);

    /**
     * Finds all habits with the specified frequency.
     * @param frequency the frequency to filter by
     * @return list of matching habits
     */
    List<Habit> findByFrequency(Habit.Frequency frequency);

    /**
     * Finds all habits whose name contains the given search term (case-insensitive).
     * @param searchTerm the term to search for in habit names
     * @return list of matching habits
     */
    List<Habit> findByNameContaining(String searchTerm);

    /**
     * Finds all habits with a best streak at least the specified minimum.
     * @param minBestStreak minimum best streak value
     * @return list of matching habits
     */
    List<Habit> findBestStreakAtLeast(int minBestStreak);

    /**
     * Finds all habits completed on the specified date.
     * @param date the completion date
     * @return list of matching habits
     */
    List<Habit> findCompletedOn(LocalDate date);

    /**
     * Finds habits that are overdue (inactive or archived for too long).
     * @return list of overdue habits
     */
    List<Habit> findOverdue();

    /**
     * Finds habits with a streak greater than the specified minimum.
     * @param minStreak minimum streak value
     * @return list of matching habits
     */
    List<Habit> findStreakGreaterThan(int minStreak);

    /**
     * Finds habits created today.
     * @return list of habits created today
     */
    List<Habit> findCreatedToday();
}