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

@DisplayName("HabitService tests")
class HabitServiceTest {

    private HabitService service;
    private HabitRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryHabitRepository();
        service = new HabitService(repository);
        repository.deleteAll();
    }

    // -------------------- Validation --------------------

    @Nested
    @DisplayName("Validation")
    class ValidationTests {

        @Test
        @DisplayName("Reject null habit")
        void rejectsNull() {
            assertThatThrownBy(() -> service.validateEntity(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be null");
        }

        @Test
        @DisplayName("Reject missing name")
        void rejectsMissingName() {
            Habit h = new Habit();
            h.setName("   ");
            h.setFrequency(Habit.Frequency.DAILY);
            h.setTargetPerWeek(7);

            assertThatThrownBy(() -> service.validateEntity(h))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("name is required");
        }

        @Test
        @DisplayName("Reject invalid targetPerWeek")
        void rejectsInvalidTarget() {
            Habit h = new Habit();
            h.setName("Read");
            h.setFrequency(Habit.Frequency.WEEKLY);
            h.setTargetPerWeek(0);

            assertThatThrownBy(() -> service.validateEntity(h))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("between 1 and 7");
        }

        @Test
        @DisplayName("Accept valid habit")
        void acceptsValid() {
            Habit h = new Habit("Read", "Read nightly", Habit.Frequency.DAILY, 7);
            assertThatNoException().isThrownBy(() -> service.validateEntity(h));
        }
    }

    // -------------------- CRUD --------------------

    @Nested
    @DisplayName("CRUD")
    class CrudTests {

        @Test
        @DisplayName("Save habit assigns id and increments count")
        void saveAssignsId() {
            Habit h = new Habit("Read", "Read nightly", Habit.Frequency.DAILY, 7);

            Habit saved = service.save(h);

            assertThat(saved.getId()).isNotNull();
            assertThat(service.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("Save invalid habit throws")
        void saveInvalidThrows() {
            Habit h = new Habit();
            h.setName("");
            h.setFrequency(Habit.Frequency.DAILY);
            h.setTargetPerWeek(7);

            assertThatThrownBy(() -> service.save(h))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThat(service.count()).isZero();
        }

        @Test
        @DisplayName("Find by id returns present/empty")
        void findByIdWorks() {
            Habit saved = service.save(new Habit("Read", "Desc", Habit.Frequency.DAILY, 7));

            assertThat(service.findById(saved.getId())).isPresent();
            assertThat(service.findById(999L)).isEmpty();
        }

        @Test
        @DisplayName("Delete by id removes habit")
        void deleteByIdWorks() {
            Habit saved = service.save(new Habit("Read", "Desc", Habit.Frequency.DAILY, 7));
            Long id = saved.getId();

            service.deleteById(id);

            assertThat(service.findById(id)).isEmpty();
        }

        @Test
        @DisplayName("Delete non-existent throws")
        void deleteMissingThrows() {
            assertThatThrownBy(() -> service.deleteById(999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("not found");
        }
    }

    // -------------------- Domain queries/helpers --------------------

    @Nested
    @DisplayName("Domain queries & helpers")
    class DomainTests {

        @BeforeEach
        void seed() {
            // Active daily habit
            Habit h1 = new Habit("Read", "Read nightly", Habit.Frequency.DAILY, 7);

            // Weekly habit (active)
            Habit h2 = new Habit("Gym", "Lift 3x/week", Habit.Frequency.WEEKLY, 3);

            // Custom habit, already archived
            Habit h3 = new Habit("Meditate", "Short sessions", Habit.Frequency.CUSTOM, 4);
            h3.setArchived(true);

            // Weekly habit last completed long ago, will be archived by rule
            Habit h4 = new Habit("Run", "Run 2x/week", Habit.Frequency.WEEKLY, 2);
            h4.setLastCompleted(LocalDate.now().minusDays(21));

            service.save(h1);
            service.save(h2);
            service.save(h3);
            service.save(h4);
        }

        @Test
        @DisplayName("findByFrequency filters correctly")
        void findByFrequency() {
            List<Habit> weekly = service.findByFrequency(Habit.Frequency.WEEKLY);
            assertThat(weekly).extracting(Habit::getName)
                    .containsExactlyInAnyOrder("Gym", "Run");
        }

        @Test
        @DisplayName("findByArchived filters correctly")
        void findByArchived() {
            List<Habit> archived = service.findByArchived(true);
            assertThat(archived).extracting(Habit::getName)
                    .containsExactlyInAnyOrder("Meditate");
        }

        @Test
        @DisplayName("groupByFrequency groups correctly")
        void groupByFrequency() {
            Map<Habit.Frequency, List<Habit>> grouped = service.groupByFrequency();
            assertThat(grouped.get(Habit.Frequency.DAILY)).extracting(Habit::getName)
                    .containsExactly("Read");
            assertThat(grouped.get(Habit.Frequency.WEEKLY)).extracting(Habit::getName)
                    .containsExactlyInAnyOrder("Gym", "Run");
            assertThat(grouped.get(Habit.Frequency.CUSTOM)).extracting(Habit::getName)
                    .containsExactly("Meditate");
        }

        @Test
        @DisplayName("countByFrequency counts correctly")
        void countByFrequency() {
            Map<Habit.Frequency, Long> counts = service.countByFrequency();
            assertThat(counts.get(Habit.Frequency.DAILY)).isEqualTo(1);
            assertThat(counts.get(Habit.Frequency.WEEKLY)).isEqualTo(2);
            assertThat(counts.get(Habit.Frequency.CUSTOM)).isEqualTo(1);
        }

        @Test
        @DisplayName("countByArchived counts active vs archived")
        void countByArchived() {
            Map<Boolean, Long> counts = service.countByArchived();
            assertThat(counts.get(false)).isEqualTo(3); // active
            assertThat(counts.get(true)).isEqualTo(1);  // archived
        }

        @Test
        @DisplayName("search looks in name & description (case-insensitive)")
        void search() {
            List<Habit> byName = service.search("read");
            assertThat(byName).extracting(Habit::getName)
                    .containsExactly("Read");

            List<Habit> byDesc = service.search("sessions");
            assertThat(byDesc).extracting(Habit::getName)
                    .containsExactly("Meditate");
        }

        @Test
        @DisplayName("completeToday bumps current/best streak")
        void completeToday() {
            Habit read = service.findByNameContaining("read").get(0);
            int before = read.getCurrentStreak();

            Habit updated = service.completeToday(read.getId());
            assertThat(updated.getCurrentStreak()).isEqualTo(Math.max(1, before + 1));
            assertThat(updated.getBestStreak()).isGreaterThanOrEqualTo(updated.getCurrentStreak());
        }

        @Test
        @DisplayName("archiveInactiveHabits archives habits older than cutoff")
        void archiveInactiveHabits() {
            int changed = service.archiveInactiveHabits(14); // > 2 weeks
            assertThat(changed).isGreaterThanOrEqualTo(1);

            List<Habit> archived = service.findByArchived(true);
            assertThat(archived).extracting(Habit::getName)
                    .contains("Meditate", "Run");
        }
    }
}


