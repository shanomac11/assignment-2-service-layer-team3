package edu.trincoll.service;

import edu.trincoll.model.Habit;
import edu.trincoll.repository.HabitRepository;
import edu.trincoll.repository.Repository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TODO: AI Collaboration Summary goes here
 * 
 * TODO: Rename this class to match your domain
 * 
 * Service layer implementing business logic.
 * Extends BaseService for common CRUD operations.
 */
@Service
public class HabitService extends BaseService<Habit, Long> {

    private final HabitRepository habitRepository;

    public HabitService(HabitRepository habitRepository) {
        this.habitRepository = habitRepository;
    }

    @Override
    protected Repository<Habit, Long> getRepository() {
        return habitRepository;
    }

    /**
     * AI Collaboration Report:
     * - AI Tool Used: ChatGPT
     * - Most Helpful Prompt: “Show a complete HabitService matching my Habit/HabitRepository”
     * - AI Mistake We Fixed: Removed leftover Item methods; fixed typos and collectors
     * - Time Saved: ~1–2 hours
     * - Team Members: <add names>
     */
    @Override
    public void validateEntity(Habit habit) {
        if (habit == null) throw new IllegalArgumentException("Habit cannot be null");
        if (habit.getName() == null || habit.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Habit name is required");
        }
        if (habit.getFrequency() == null) {
            throw new IllegalArgumentException("Frequency is required");
        }
        if (habit.getTargetPerWeek() < 1 || habit.getTargetPerWeek() > 7) {
            throw new IllegalArgumentException("Target per week must be between 1 and 7");
        }
        if (habit.getCurrentStreak() < 0 || habit.getBestStreak() < 0) {
            throw new IllegalArgumentException("Streaks cannot be negative");
        }
        habit.setName(habit.getName().trim()); // normalize
    }

    // ---------- Query pass-throughs ----------
    public List<Habit> findByArchived(boolean archived) {
        return habitRepository.findByArchived(archived);
    }

    public List<Habit> findByFrequency(Habit.Frequency frequency) {
        return habitRepository.findByFrequency(frequency);
    }

    public List<Habit> findByNameContaining(String searchTerm) {
        return habitRepository.findByNameContaining(searchTerm);
    }

    public List<Habit> findStreakGreaterThan(int minStreak) {
        return habitRepository.findStreakGreaterThan(minStreak);
    }

    public List<Habit> findBestStreakAtLeast(int minBestStreak) {
        return habitRepository.findBestStreakAtLeast(minBestStreak);
    }

    public List<Habit> findCompletedOn(LocalDate date) {
        return habitRepository.findCompletedOn(date);
    }

    public List<Habit> findCreatedToday() {
        return habitRepository.findCreatedToday();
    }

    // ---------- Domain helpers ----------
    /** Group habits by frequency. */
    public Map<Habit.Frequency, List<Habit>> groupByFrequency() {
        return findAll().stream().collect(Collectors.groupingBy(Habit::getFrequency));
    }

    /** Count habits by frequency. */
    public Map<Habit.Frequency, Long> countByFrequency() {
        return findAll().stream()
                .collect(Collectors.groupingBy(Habit::getFrequency, Collectors.counting()));
    }

    /** Count habits by archived flag (active vs archived). */
    public Map<Boolean, Long> countByArchived() {
        return findAll().stream()
                .collect(Collectors.groupingBy(Habit::isArchived, Collectors.counting()));
    }

    /** Search by name or description. */
    public List<Habit> search(String query) {
        String q = (query == null) ? "" : query.toLowerCase();
        return findAll().stream()
                .filter(h ->
                        (h.getName() != null && h.getName().toLowerCase().contains(q)) ||
                                (h.getDescription() != null && h.getDescription().toLowerCase().contains(q))
                )
                .toList();
    }

    /** Mark a habit completed today and persist streak update. */
    public Habit completeToday(Long id) {
        Habit habit = findById(id).orElseThrow(() -> new IllegalArgumentException("Habit " + id + " not found"));
        habit.completeToday();
        return save(habit);
    }

    /** Archive or unarchive a habit and persist. */
    public Habit setArchived(Long id, boolean archived) {
        Habit habit = findById(id).orElseThrow(() -> new IllegalArgumentException("Habit " + id + " not found"));
        habit.setArchived(archived);
        return save(habit);
    }

    /** Archive habits that haven't been completed in the given number of days. */
    public int archiveInactiveHabits(int daysWithoutCompletion) {
        if (daysWithoutCompletion < 1) throw new IllegalArgumentException("daysWithoutCompletion must be >= 1");
        LocalDate cutoff = LocalDate.now().minusDays(daysWithoutCompletion);
        List<Habit> toArchive = findAll().stream()
                .filter(h -> !h.isArchived())
                .filter(h -> h.getLastCompleted() == null || h.getLastCompleted().isBefore(cutoff))
                .toList();
        toArchive.forEach(h -> h.setArchived(true));
        saveAll(toArchive);
        return toArchive.size();
    }

    public List<Habit> findOverdue() {
        return habitRepository.findOverdue();
    }
}