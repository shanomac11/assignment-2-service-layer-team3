package edu.trincoll.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class HabitModelTest {

    @Test
    @DisplayName("completeToday increments and resets streaks correctly")
    void completeTodayUpdatesStreaks() {
        Habit h = new Habit("Read", "", Habit.Frequency.DAILY, 7);

        // First completion sets streaks to 1
        h.completeToday();
        assertThat(h.getCurrentStreak()).isEqualTo(1);
        assertThat(h.getBestStreak()).isEqualTo(1);
        assertThat(h.getLastCompleted()).isEqualTo(LocalDate.now());

        // Simulate yesterday completion and complete again today → consecutive day
        h.setLastCompleted(LocalDate.now().minusDays(1));
        h.setCurrentStreak(1);
        h.setBestStreak(1);
        h.completeToday();
        assertThat(h.getCurrentStreak()).isEqualTo(2);
        assertThat(h.getBestStreak()).isEqualTo(2);

        // Simulate a gap > 1 day → streak resets to 1 but best remains
        h.setLastCompleted(LocalDate.now().minusDays(3));
        h.setCurrentStreak(2);
        h.setBestStreak(3);
        h.completeToday();
        assertThat(h.getCurrentStreak()).isEqualTo(1);
        assertThat(h.getBestStreak()).isEqualTo(3);
    }

    @Test
    @DisplayName("Negative streak setters clamp to zero")
    void negativeStreaksClampToZero() {
        Habit h = new Habit("Walk", "", Habit.Frequency.DAILY, 7);
        h.setCurrentStreak(-5);
        h.setBestStreak(-9);
        assertThat(h.getCurrentStreak()).isZero();
        assertThat(h.getBestStreak()).isZero();
    }

    @Test
    @DisplayName("Equality and hashCode prefer id when present")
    void equalityPrefersId() {
        Habit a = new Habit("A", "", Habit.Frequency.DAILY, 7);
        Habit b = new Habit("B", "", Habit.Frequency.DAILY, 7);

        // Without ids, equality falls back to name+createdAt
        b.setName(a.getName());
        b.setCreatedAt(a.getCreatedAt());
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());

        // With ids, equality uses id only
        a.setId(1L);
        b.setId(2L);
        assertThat(a).isNotEqualTo(b);
        b.setId(1L);
        assertThat(a).isEqualTo(b);
    }

    @Test
    @DisplayName("toString contains key fields")
    void toStringContainsFields() {
        Habit h = new Habit("Code", "Practice", Habit.Frequency.WEEKLY, 2);
        h.setId(42L);
        h.setCreatedAt(LocalDateTime.now());
        String s = h.toString();
        assertThat(s).contains("Code").contains("WEEKLY").contains("42");
    }
}

