package edu.trincoll.service;

import edu.trincoll.model.Habit;
import edu.trincoll.repository.HabitRepository;
import edu.trincoll.repository.InMemoryHabitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class HabitServiceTest {

    private HabitService service;
    private HabitRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryHabitRepository();
        service = new HabitService(repository);
        repository.deleteAll();
    }

    @Nested
    @DisplayName("Validation")
    class ValidationTests {
        @Test
        @DisplayName("Reject null habit")
        void rejectNullHabit() {
            assertThatThrownBy(() -> service.validateEntity(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be null");
        }

        @Test
        @DisplayName("Reject habit without name")
        void rejectNoName() {
            Habit h = new Habit();
            assertThatThrownBy(() -> service.validateEntity(h))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("name is required");
        }

        @Test
        @DisplayName("Reject invalid targetPerWeek")
        void rejectInvalidTarget() {
            Habit h = new Habit("Test", "", Habit.Frequency.DAILY, 0);
            assertThatThrownBy(() -> service.validateEntity(h))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("between 1 and 7");
        }

        @Test
        @DisplayName("Reject null frequency")
        void rejectNullFrequency() {
            Habit h = new Habit("Test", "", Habit.Frequency.DAILY, 7);
            h.setFrequency(null);
            assertThatThrownBy(() -> service.validateEntity(h))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Frequency is required");
        }

        @Test
        @DisplayName("Accept valid habit")
        void acceptValidHabit() {
            Habit h = new Habit("Valid", "Desc", Habit.Frequency.WEEKLY, 2);
            assertThatNoException().isThrownBy(() -> service.validateEntity(h));
        }
    }

    @Nested
    @DisplayName("CRUD")
    class CrudTests {
        @Test
        @DisplayName("findById(null) throws and existsById(null) is false")
        void nullIdGuards() {
            assertThatThrownBy(() -> service.findById(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ID cannot be null");
            assertThat(service.existsById(null)).isFalse();
        }
        @Test
        @DisplayName("Save with validation")
        void saveWithValidation() {
            Habit h = new Habit("Exercise", "Daily", Habit.Frequency.DAILY, 7);
            Habit saved = service.save(h);
            assertThat(saved.getId()).isNotNull();
            assertThat(service.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("Not save invalid")
        void notSaveInvalid() {
            Habit h = new Habit("", "", Habit.Frequency.DAILY, 7);
            assertThatThrownBy(() -> service.save(h)).isInstanceOf(IllegalArgumentException.class);
            assertThat(service.count()).isZero();
        }

        @Test
        @DisplayName("Find by id and delete")
        void findAndDelete() {
            Habit h = service.save(new Habit("A", "", Habit.Frequency.DAILY, 7));
            assertThat(service.findById(h.getId())).isPresent();

            service.deleteById(h.getId());
            assertThat(service.findById(h.getId())).isEmpty();
        }

        @Test
        @DisplayName("Delete non-existent throws")
        void deleteNonExistent() {
            assertThatThrownBy(() -> service.deleteById(999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("not found");
        }
    }

    @Nested
    @DisplayName("Queries & Helpers")
    class QueryTests {
        @BeforeEach
        void seed() {
            Habit h1 = new Habit("Gym", "", Habit.Frequency.DAILY, 7);
            Habit h2 = new Habit("Review", "", Habit.Frequency.WEEKLY, 1);
            Habit h3 = new Habit("Walk", "Evening walk", Habit.Frequency.DAILY, 7);
            h2.setArchived(true);
            service.save(h1);
            service.save(h2);
            service.save(h3);
        }

        @Test
        @DisplayName("Find by archived")
        void findByArchived() {
            assertThat(service.findByArchived(true)).extracting(Habit::getName).containsExactly("Review");
        }

        @Test
        @DisplayName("Find by frequency")
        void findByFrequency() {
            assertThat(service.findByFrequency(Habit.Frequency.DAILY)).hasSize(2);
        }

        @Test
        @DisplayName("Group and count by frequency")
        void groupAndCountByFrequency() {
            Map<Habit.Frequency, List<Habit>> grouped = service.groupByFrequency();
            Map<Habit.Frequency, Long> counts = service.countByFrequency();
            assertThat(grouped.get(Habit.Frequency.DAILY)).hasSize(2);
            assertThat(counts.get(Habit.Frequency.DAILY)).isEqualTo(2);
        }

        @Test
        @DisplayName("Count by archived flag")
        void countByArchived() {
            Map<Boolean, Long> counts = service.countByArchived();
            assertThat(counts.get(true)).isEqualTo(1);
            assertThat(counts.get(false)).isEqualTo(2);
        }

        @Test
        @DisplayName("Search name/description")
        void search() {
            assertThat(service.search("walk")).extracting(Habit::getName).containsExactly("Walk");
        }

        @Test
        @DisplayName("Complete today updates streaks")
        void completeToday() {
            Habit h = service.save(new Habit("Streak", "", Habit.Frequency.DAILY, 7));
            Habit updated = service.completeToday(h.getId());
            assertThat(updated.getCurrentStreak()).isGreaterThanOrEqualTo(1);
            assertThat(updated.getBestStreak()).isGreaterThanOrEqualTo(1);
            assertThat(updated.getLastCompleted()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("Set archived toggles flag")
        void setArchived() {
            Habit h = service.save(new Habit("Archive", "", Habit.Frequency.DAILY, 7));
            Habit archived = service.setArchived(h.getId(), true);
            assertThat(archived.isArchived()).isTrue();
        }

        @Test
        @DisplayName("Archive inactive habits")
        void archiveInactiveHabits() {
            // Clear any seeded data from QueryTests setup to isolate this scenario
            service.deleteAll();
            Habit active = new Habit("Active", "", Habit.Frequency.DAILY, 7);
            active.setLastCompleted(LocalDate.now());
            Habit inactive = new Habit("Inactive", "", Habit.Frequency.DAILY, 7);
            inactive.setLastCompleted(LocalDate.now().minusDays(30));
            Habit never = new Habit("Never", "", Habit.Frequency.DAILY, 7);

            service.save(active);
            service.save(inactive);
            service.save(never);

            int archived = service.archiveInactiveHabits(7);
            assertThat(archived).isEqualTo(2);
            assertThat(service.findByArchived(true)).extracting(Habit::getName)
                    .containsExactlyInAnyOrder("Inactive", "Never");
        }
    }
}
