package edu.trincoll.repository;

import java.time.LocalDate;
import edu.trincoll.model.Habit;
import java.util.List;

/**
 * TODO: Rename this interface to match your domain
 * Examples: BookmarkRepository, QuoteRepository, etc.
 * 
 * Add domain-specific query methods that make sense for your use case.
 */
public interface HabitRepository extends Repository<Habit, Long> {
    
    /**
     * Find all items with a specific status
     */
    //List<Habit> findByStatus(Habit.Status status);

    List<Habit> findByArchived(boolean archived);
    List<Habit> findByFrequency(Habit.Frequency frequency);
    List<Habit> findByNameContaining(String searchTerm);
    List<Habit> findBestStreakAtLeast(int minBestStreak);
    List<Habit> findCompletedOn(LocalDate date);
    /**
     * Find all items in a category
     */
    //List<Habit> findByCategory(String category);
    
    /**
     * Find all items containing a specific tag
     */
   //List<Habit> findByTag(String tag);
    
    /**
     * Find items with title containing search term (case-insensitive)
     */
    //List<Habit> findByTitleContaining(String searchTerm);
    
    /**
     * TODO: Add at least 3 more domain-specific query methods (COMPLETED!)
     * Examples:
     * - findByAuthor(String author) for quotes
     * - findByUrl(String url) for bookmarks  
     * - findOverdue() for habits
     * - findByIngredient(String ingredient) for recipes
     */

    /**
     * Find habits that are overdue (Inactive/Archived for too long)
     */
    List<Habit> findOverdue();

    //Find habits by minimum streak length (add streak field later)
    List<Habit> findStreakGreaterThan(int minStreak);

    //Find habits created today
    List<Habit> findCreatedToday();
}