package edu.trincoll.controller;

import edu.trincoll.model.Habit;
import edu.trincoll.service.HabitService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST controller for Habit domain.
 * Handles HTTP requests for habit tracking.
 * All business logic is delegated to the service layer.
 */
@RestController
@RequestMapping("/api/habits")
public class HabitController {

    private final HabitService service;

    public HabitController(HabitService service) {
        this.service = service;
    }

    @GetMapping
    public List<Habit> getAllHabits() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Habit> getHabitById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Habit> createHabit(@RequestBody Habit habit) {
        try {
            Habit saved = service.save(habit);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Habit> updateHabit(@PathVariable Long id, @RequestBody Habit habit) {
        if (!service.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        habit.setId(id);
        try {
            Habit updated = service.save(habit);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHabit(@PathVariable Long id) {
        try {
            service.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Additional endpoints for habit-specific queries

    @GetMapping("/archived/{archived}")
    public List<Habit> getHabitsByArchived(@PathVariable boolean archived) {
        return service.findByArchived(archived);
    }

    @GetMapping("/frequency/{frequency}")
    public List<Habit> getHabitsByFrequency(@PathVariable Habit.Frequency frequency) {
        return service.findByFrequency(frequency);
    }

    @GetMapping("/search")
    public List<Habit> searchHabits(@RequestParam String query) {
        return service.findByNameContaining(query);
    }

    @GetMapping("/streak/best/{minBestStreak}")
    public List<Habit> getHabitsByBestStreak(@PathVariable int minBestStreak) {
        return service.findBestStreakAtLeast(minBestStreak);
    }

    @GetMapping("/completed/{date}")
    public List<Habit> getHabitsCompletedOn(@PathVariable LocalDate date) {
        return service.findCompletedOn(date);
    }

    @GetMapping("/overdue")
    public List<Habit> getOverdueHabits() {
        return service.findOverdue();
    }

    @GetMapping("/streak/current/{minStreak}")
    public List<Habit> getHabitsByCurrentStreak(@PathVariable int minStreak) {
        return service.findStreakGreaterThan(minStreak);
    }

    @GetMapping("/created/today")
    public List<Habit> getHabitsCreatedToday() {
        return service.findCreatedToday();
    }
}