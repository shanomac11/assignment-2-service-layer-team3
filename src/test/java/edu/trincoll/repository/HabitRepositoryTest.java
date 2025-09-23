package edu.trincoll.repository;

import edu.trincoll.model.Habit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

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
        Habit habit = new Habit("Exercise", "Daily workout", Habit.Frequency.DAILY, 7);

        Habit saved = repository.save(habit);
        assertThat(saved.getId()).isNotNull();

        Optional<Habit> found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Exercise");
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
        repository.save(new Habit("Meditate", "Morning", Habit.Frequency.DAILY, 7));
        repository.save(new Habit("Read", "Books", Habit.Frequency.DAILY, 7));
        repository.save(new Habit("Review", "Weekly review", Habit.Frequency.WEEKLY, 1));

        List<Habit> all = repository.findAll();
        assertThat(all).hasSize(3);
        assertThat(all).extracting(Habit::getName)
                .containsExactlyInAnyOrder("Meditate", "Read", "Review");
    }

    @Test
    @DisplayName("Should delete habit by ID")
    void testDeleteById() {
        Habit habit = repository.save(new Habit("Temp", "To delete", Habit.Frequency.DAILY, 7));
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
        repository.save(new Habit("A", "", Habit.Frequency.DAILY, 7));
        repository.save(new Habit("B", "", Habit.Frequency.DAILY, 7));
        assertThat(repository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should delete all habits")
    void testDeleteAll() {
        repository.save(new Habit("A", "", Habit.Frequency.DAILY, 7));
        repository.save(new Habit("B", "", Habit.Frequency.DAILY, 7));
        assertThat(repository.count()).isEqualTo(2);

        repository.deleteAll();
        assertThat(repository.count()).isZero();
        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("Should save multiple habits")
    void testSaveAll() {
        List<Habit> habits = List.of(
                new Habit("A", "", Habit.Frequency.DAILY, 7),
                new Habit("B", "", Habit.Frequency.WEEKLY, 2),
                new Habit("C", "", Habit.Frequency.CUSTOM, 3)
        );
        List<Habit> saved = repository.saveAll(habits);
        assertThat(saved).hasSize(3);
        assertThat(saved).allMatch(h -> h.getId() != null);
        assertThat(repository.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should find by archived flag")
    void testFindByArchived() {
        Habit a = new Habit("A", "", Habit.Frequency.DAILY, 7);
        Habit b = new Habit("B", "", Habit.Frequency.DAILY, 7);
        b.setArchived(true);
        repository.save(a);
        repository.save(b);

        assertThat(repository.findByArchived(true)).extracting(Habit::getName).containsExactly("B");
        assertThat(repository.findByArchived(false)).extracting(Habit::getName).containsExactly("A");
    }

    @Test
    @DisplayName("Should find by frequency")
    void testFindByFrequency() {
        repository.save(new Habit("Gym", "", Habit.Frequency.DAILY, 7));
        repository.save(new Habit("Weekly Review", "", Habit.Frequency.WEEKLY, 1));

        assertThat(repository.findByFrequency(Habit.Frequency.DAILY))
                .extracting(Habit::getName).contains("Gym");

        // Null frequency returns empty list
        assertThat(repository.findByFrequency(null)).isEmpty();
    }

    @Test
    @DisplayName("Should find by name containing term")
    void testFindByNameContaining() {
        repository.save(new Habit("Java Practice", "", Habit.Frequency.DAILY, 7));
        repository.save(new Habit("Python Practice", "", Habit.Frequency.DAILY, 7));
        repository.save(new Habit("Walk", "", Habit.Frequency.DAILY, 7));

        List<Habit> results = repository.findByNameContaining("practice");
        assertThat(results).extracting(Habit::getName)
                .containsExactlyInAnyOrder("Java Practice", "Python Practice");
    }

    @Test
    @DisplayName("Should find by best streak at least")
    void testFindBestStreakAtLeast() {
        Habit a = new Habit("A", "", Habit.Frequency.DAILY, 7);
        a.setBestStreak(5);
        Habit b = new Habit("B", "", Habit.Frequency.DAILY, 7);
        b.setBestStreak(2);
        repository.save(a);
        repository.save(b);
        assertThat(repository.findBestStreakAtLeast(3)).extracting(Habit::getName)
                .containsExactly("A");
    }

    @Test
    @DisplayName("Should find completed on specific date")
    void testFindCompletedOn() {
        LocalDate today = LocalDate.now();
        Habit a = new Habit("A", "", Habit.Frequency.DAILY, 7);
        a.setLastCompleted(today);
        Habit b = new Habit("B", "", Habit.Frequency.DAILY, 7);
        b.setLastCompleted(today.minusDays(1));
        repository.save(a);
        repository.save(b);
        assertThat(repository.findCompletedOn(today)).extracting(Habit::getName)
                .containsExactly("A");

        // Null date returns empty list
        assertThat(repository.findCompletedOn(null)).isEmpty();
    }

    @Test
    @DisplayName("Should find overdue habits")
    void testFindOverdue() {
        Habit a = new Habit("A", "", Habit.Frequency.DAILY, 7);
        a.setLastCompleted(LocalDate.now().minusDays(8));
        Habit b = new Habit("B", "", Habit.Frequency.DAILY, 7);
        b.setLastCompleted(LocalDate.now().minusDays(3));
        repository.save(a);
        repository.save(b);
        assertThat(repository.findOverdue()).extracting(Habit::getName)
                .containsExactly("A");
    }

    @Test
    @DisplayName("Should find by current streak greater than threshold")
    void testFindStreakGreaterThan() {
        Habit a = new Habit("A", "", Habit.Frequency.DAILY, 7);
        a.setCurrentStreak(4);
        Habit b = new Habit("B", "", Habit.Frequency.DAILY, 7);
        b.setCurrentStreak(1);
        repository.save(a);
        repository.save(b);
        assertThat(repository.findStreakGreaterThan(3)).extracting(Habit::getName)
                .containsExactly("A");
    }

    @Test
    @DisplayName("Should find habits created today")
    void testFindCreatedToday() {
        Habit today = new Habit("Today", "", Habit.Frequency.DAILY, 7);
        Habit yesterday = new Habit("Yesterday", "", Habit.Frequency.DAILY, 7);
        yesterday.setCreatedAt(LocalDateTime.now().minusDays(1));
        repository.save(today);
        repository.save(yesterday);
        assertThat(repository.findCreatedToday()).extracting(Habit::getName)
                .containsExactly("Today");
    }
}
