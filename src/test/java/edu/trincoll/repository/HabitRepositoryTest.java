package edu.trincoll.repository;

import edu.trincoll.model.Habit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for the repository layer.
 * These tests align with the methods in HabitRepository and InMemoryHabitRepository.
 */
class HabitRepositoryTest {

    private HabitRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryHabitRepository();
        repository.deleteAll();
    }

    @Test
    @DisplayName("Should save and retrieve habit by ID")
    void testSaveAndFindById() {
        Habit habit = new Habit("Test Habit", "Description", Habit.Frequency.DAILY, 7);

        Habit saved = repository.save(habit);

        assertThat(saved.getId()).isNotNull();

        Optional<Habit> found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Habit");
    }

    @Test
    @DisplayName("Should return empty Optional for non-existent ID")
    void testFindByIdNotFound() {
        Optional<Habit> found = repository.findById(999L);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find all habits")
    void testFindAll() {
        repository.save(new Habit("Habit 1", "Desc 1", Habit.Frequency.DAILY, 7));
        repository.save(new Habit("Habit 2", "Desc 2", Habit.Frequency.WEEKLY, 2));
        repository.save(new Habit("Habit 3", "Desc 3", Habit.Frequency.WEEKLY, 1)); // changed from MONTHLY

        List<Habit> all = repository.findAll();

        assertThat(all).hasSize(3);
        assertThat(all).extracting(Habit::getName)
                .containsExactlyInAnyOrder("Habit 1", "Habit 2", "Habit 3");
    }

    @Test
    @DisplayName("Should delete habit by ID")
    void testDeleteById() {
        Habit habit = repository.save(new Habit("To Delete", "Will be deleted", Habit.Frequency.DAILY, 7));
        Long id = habit.getId();

        assertThat(repository.existsById(id)).isTrue();

        repository.deleteById(id);

        assertThat(repository.existsById(id)).isFalse();
        assertThat(repository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("Should check if habit exists")
    void testExistsById() {
        Habit habit = repository.save(new Habit("Exists", "Test", Habit.Frequency.DAILY, 7));

        assertThat(repository.existsById(habit.getId())).isTrue();
        assertThat(repository.existsById(999L)).isFalse();
    }

    @Test
    @DisplayName("Should count habits correctly")
    void testCount() {
        assertThat(repository.count()).isZero();

        repository.save(new Habit("Habit 1", "Desc", Habit.Frequency.DAILY, 7));
        repository.save(new Habit("Habit 2", "Desc", Habit.Frequency.WEEKLY, 2));

        assertThat(repository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should delete all habits")
    void testDeleteAll() {
        repository.save(new Habit("Habit 1", "Desc", Habit.Frequency.DAILY, 7));
        repository.save(new Habit("Habit 2", "Desc", Habit.Frequency.WEEKLY, 2));

        assertThat(repository.count()).isEqualTo(2);

        repository.deleteAll();

        assertThat(repository.count()).isZero();
        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("Should save multiple habits")
    void testSaveAll() {
        List<Habit> habits = List.of(
                new Habit("Habit 1", "Desc 1", Habit.Frequency.DAILY, 7),
                new Habit("Habit 2", "Desc 2", Habit.Frequency.WEEKLY, 2),
                new Habit("Habit 3", "Desc 3", Habit.Frequency.WEEKLY, 1) // changed from MONTHLY
        );

        List<Habit> saved = repository.saveAll(habits);

        assertThat(saved).hasSize(3);
        assertThat(saved).allMatch(h -> h.getId() != null);
        assertThat(repository.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should find habits by archived status")
    void testFindByArchived() {
        Habit active = new Habit("Active", "Active habit", Habit.Frequency.DAILY, 7);
        active.setArchived(false);

        Habit archived = new Habit("Archived", "Archived habit", Habit.Frequency.WEEKLY, 2);
        archived.setArchived(true);

        repository.save(active);
        repository.save(archived);

        assertThat(repository.findByArchived(false)).extracting(Habit::getName)
                .containsExactly("Active");
        assertThat(repository.findByArchived(true)).extracting(Habit::getName)
                .containsExactly("Archived");
    }

    @Test
    @DisplayName("Should find habits by frequency")
    void testFindByFrequency() {
        Habit habit1 = new Habit("Habit 1", "Desc", Habit.Frequency.DAILY, 7);
        Habit habit2 = new Habit("Habit 2", "Desc", Habit.Frequency.WEEKLY, 2);
        Habit habit3 = new Habit("Habit 3", "Desc", Habit.Frequency.DAILY, 7);

        repository.save(habit1);
        repository.save(habit2);
        repository.save(habit3);

        List<Habit> dailyHabits = repository.findByFrequency(Habit.Frequency.DAILY);

        assertThat(dailyHabits).hasSize(2);
        assertThat(dailyHabits).extracting(Habit::getName)
                .containsExactlyInAnyOrder("Habit 1", "Habit 3");
    }

    @Test
    @DisplayName("Should find habits by name containing search term")
    void testFindByNameContaining() {
        repository.save(new Habit("Morning Run", "Exercise", Habit.Frequency.DAILY, 7));
        repository.save(new Habit("Evening Read", "Book", Habit.Frequency.WEEKLY, 2));
        repository.save(new Habit("Run Errands", "Chores", Habit.Frequency.WEEKLY, 1)); // changed from MONTHLY
        repository.save(new Habit("Meditation", "Mindfulness", Habit.Frequency.DAILY, 7));

        List<Habit> runHabits = repository.findByNameContaining("Run");

        assertThat(runHabits).hasSize(2);
        assertThat(runHabits).extracting(Habit::getName)
                .containsExactlyInAnyOrder("Morning Run", "Run Errands");
    }

    @Test
    @DisplayName("Should find habits by best streak")
    void testFindBestStreakAtLeast() {
        Habit h1 = new Habit("Habit 1", "Desc", Habit.Frequency.DAILY, 7);
        h1.setBestStreak(5);

        Habit h2 = new Habit("Habit 2", "Desc", Habit.Frequency.DAILY, 7);
        h2.setBestStreak(2);

        repository.save(h1);
        repository.save(h2);

        List<Habit> result = repository.findBestStreakAtLeast(3);
        assertThat(result).extracting(Habit::getName).containsExactly("Habit 1");
    }

    @Test
    @DisplayName("Should find habits completed on a date")
    void testFindCompletedOn() {
        Habit habit = new Habit("Habit", "Desc", Habit.Frequency.DAILY, 7);
        habit.setLastCompleted(LocalDate.now());
        repository.save(habit);

        List<Habit> results = repository.findCompletedOn(LocalDate.now());
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Habit");
    }

    @Test
    @DisplayName("Should find overdue habits")
    void testFindOverdue() {
        Habit habit = new Habit("Overdue Habit", "Desc", Habit.Frequency.DAILY, 7);
        habit.setLastCompleted(LocalDate.now().minusDays(10));
        repository.save(habit);

        List<Habit> results = repository.findOverdue();
        assertThat(results).extracting(Habit::getName).containsExactly("Overdue Habit");
    }

    @Test
    @DisplayName("Should find habits with streak greater than")
    void testFindStreakGreaterThan() {
        Habit h1 = new Habit("Habit 1", "Desc", Habit.Frequency.DAILY, 7);
        h1.setCurrentStreak(3);

        Habit h2 = new Habit("Habit 2", "Desc", Habit.Frequency.DAILY, 7);
        h2.setCurrentStreak(1);

        repository.save(h1);
        repository.save(h2);

        List<Habit> results = repository.findStreakGreaterThan(2);
        assertThat(results).extracting(Habit::getName).containsExactly("Habit 1");
    }

    @Test
    @DisplayName("Should find habits created today")
    void testFindCreatedToday() {
        Habit habit = new Habit("Today Habit", "Desc", Habit.Frequency.DAILY, 7);
        repository.save(habit);

        List<Habit> results = repository.findCreatedToday();
        assertThat(results).isNotEmpty();
    }
}
