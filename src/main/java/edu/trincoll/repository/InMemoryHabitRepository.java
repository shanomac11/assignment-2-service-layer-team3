package edu.trincoll.repository;

import edu.trincoll.model.Habit;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * TODO: Rename this class to match your domain (COMPLETED!)
 * 
 * In-memory implementation of the repository using Java collections.
 * Uses ConcurrentHashMap for thread-safety.
 */
@Repository
public class InMemoryHabitRepository implements HabitRepository {
    
    private final Map<Long, Habit> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    @Override
    public Habit save(Habit entity) {
        if (entity.getId() == null) {
            entity.setId(idGenerator.getAndIncrement());
        }
        //defensive copy storage
        Habit copy = copyOf(entity);
        storage.put(copy.getId(), copy);
        return copyOf(copy);
    }
    
    @Override
    public Optional<Habit> findById(Long id) {
        Habit found = storage.get(id);
        return Optional.ofNullable(found == null ? null : copyOf(found));
    }
    
    @Override
    public List<Habit> findAll() {
        // TODO: Return defensive copy (COMPLETED!)
        return storage.values().stream()
                .sorted(Comparator.comparing(Habit::getId))
                .map(this::copyOf)
                .collect(Collectors.toUnmodifiableList());
        //return new ArrayList<>(storage.values());
    }
    
    @Override
    public void deleteById(Long id) {storage.remove(id); }
    
    @Override
    public boolean existsById(Long id) {
        return storage.containsKey(id);
    }
    
    @Override
    public long count() {
        return storage.size();
    }
    
    @Override
    public void deleteAll() {
        storage.clear();
        idGenerator.set(1);
    }
    
    @Override
    public List<Habit> saveAll(List<Habit> entities) {
        if(entities==null || entities.isEmpty()) return List.of();
        List<Habit> saved = new ArrayList<>(entities.size());
        for (Habit h : entities) saved.add(save(h));
        return Collections.unmodifiableList(saved);
    }
    
    @Override
    public List<Habit> findByArchived(boolean archived) {
        return storage.values().stream()
                .filter(h -> h.isArchived() == archived)
                .sorted(Comparator.comparing(Habit::getId))
                .map(this::copyOf)
                .collect(Collectors.toUnmodifiableList());
    }
    
    @Override
    public List<Habit> findByFrequency(Habit.Frequency frequency) {
        // TODO: Implement (COMPLETED!)
        if (frequency == null) return List.of();
        return storage.values().stream()
                .filter(h -> frequency.equals(h.getFrequency()))
                .sorted(Comparator.comparing(Habit::getId))
                .map(this ::copyOf)
                .collect(Collectors.toUnmodifiableList());
        //return Collections.emptyList();
    }
    
    @Override
    public List<Habit> findByNameContaining(String searchTerm) {
        String q = searchTerm == null ? "" : searchTerm.toLowerCase();
        return storage.values().stream()
                .filter(h -> h.getName() != null && h.getName().toLowerCase().contains(q))
                .sorted(Comparator.comparing(Habit::getId))
                .map(this::copyOf)
                .collect(Collectors.toUnmodifiableList());

        //return Collections.emptyList();
    }
    
    @Override
    public List<Habit> findStreakGreaterThan(int minStreak) {
        return storage.values().stream()
                .filter(h -> h.getCurrentStreak() >= minStreak)
                .sorted(Comparator.comparing(Habit::getId))
                .map(this::copyOf)
                .collect(Collectors.toUnmodifiableList());
        // TODO: Implement case-insensitive search (COMPLETED!)
        //return Collections.emptyList();
    }

    //@Override
    public List<Habit> findBestStreakAtLeast(int minBestStreak) {
        return storage.values().stream()
                .filter(h -> h.getBestStreak() >= minBestStreak)
                .sorted(Comparator.comparing(Habit::getId))
                .map(this::copyOf)
                .collect(Collectors.toUnmodifiableList());
    }

    //@Override
    public List<Habit> findCompletedOn(LocalDate date) {
        if(date==null) return List.of();
        return storage.values().stream()
                .filter(h -> date.equals(h.getLastCompleted()))
                .sorted(Comparator.comparing(Habit::getId))
                .map(this::copyOf)
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<Habit> findCreatedToday(){
        LocalDate today = LocalDate.now();
        return storage.values().stream()
                .filter(h -> h.getCreatedAt() != null && h.getCreatedAt().toLocalDate().isEqual(today))
                .sorted(Comparator.comparing(Habit::getId))
                .map(this::copyOf)
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<Habit> findOverdue() {
        // Simple rule: treat archived habits as "overdue".
        // If you prefer time-based, see the comment below.
        return storage.values().stream()
                .filter(Habit::isArchived)
                .sorted(Comparator.comparing(Habit::getId))
                .map(this::copyOf)
                .collect(Collectors.toUnmodifiableList());

        /*
    // NOTE: Alternative time-based rule (e.g., not completed for 7+ days):
    LocalDate threshold = LocalDate.now().minusDays(7);
    return storage.values().stream()
            .filter(h -> h.getLastCompleted() != null && h.getLastCompleted().isBefore(threshold))
            .sorted(Comparator.comparing(Habit::getId))
            .map(this::copyOf)
            .collect(Collectors.toUnmodifiableList());
    */
    }

    private Habit copyOf(Habit h) {
        Habit c= new Habit();
        c.setId(h.getId());
        c.setName(h.getName());
        c.setDescription(h.getDescription());
        c.setFrequency(h.getFrequency());
        c.setTargetPerWeek(h.getTargetPerWeek());
        c.setCurrentStreak(h.getCurrentStreak());
        c.setBestStreak(h.getBestStreak());
        c.setLastCompleted(h.getLastCompleted());
        c.setCreatedAt(h.getCreatedAt());
        c.setArchived(h.isArchived());
        return c;
    }
}