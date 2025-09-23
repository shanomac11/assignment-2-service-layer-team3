package edu.trincoll.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.trincoll.model.Habit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Habit stack.
 * Tests controller → service → repository integration.
 */
@SpringBootTest
@AutoConfigureMockMvc
class HabitIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception {
        // Clear all habits before each test
        mockMvc.perform(get("/api/habits"))
                .andExpect(status().isOk())
                .andDo(result -> {
                    String content = result.getResponse().getContentAsString();
                    Habit[] habits = objectMapper.readValue(content, Habit[].class);
                    for (Habit habit : habits) {
                        mockMvc.perform(delete("/api/habits/" + habit.getId()));
                    }
                });
    }

    @Test
    @DisplayName("Should create habit via REST API")
    void testCreateHabit() throws Exception {
        Habit habit = new Habit("Exercise", "Daily workout", Habit.Frequency.DAILY, 7);

        mockMvc.perform(post("/api/habits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(habit)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Exercise"))
                .andExpect(jsonPath("$.description").value("Daily workout"))
                .andExpect(jsonPath("$.frequency").value("DAILY"));
    }

    @Test
    @DisplayName("Should reject invalid habit creation")
    void testCreateInvalidHabit() throws Exception {
        Habit habit = new Habit("", "Desc", Habit.Frequency.DAILY, 7); // Invalid: empty name

        mockMvc.perform(post("/api/habits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(habit)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get all habits")
    void testGetAllHabits() throws Exception {
        Habit h1 = new Habit("Meditate", "Morning meditation", Habit.Frequency.DAILY, 7);
        Habit h2 = new Habit("Read", "Read 30 minutes", Habit.Frequency.DAILY, 7);

        mockMvc.perform(post("/api/habits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(h1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/habits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(h2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/habits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Meditate", "Read")));
    }

    @Test
    @DisplayName("Should get habit by ID")
    void testGetHabitById() throws Exception {
        Habit habit = new Habit("Sleep", "Get 8 hours", Habit.Frequency.DAILY, 7);

        String response = mockMvc.perform(post("/api/habits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(habit)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Habit created = objectMapper.readValue(response, Habit.class);

        mockMvc.perform(get("/api/habits/" + created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()))
                .andExpect(jsonPath("$.name").value("Sleep"));
    }

    @Test
    @DisplayName("Should return 404 for non-existent habit")
    void testGetNonExistentHabit() throws Exception {
        mockMvc.perform(get("/api/habits/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should update habit")
    void testUpdateHabit() throws Exception {
        Habit habit = new Habit("Water", "Drink water", Habit.Frequency.DAILY, 7);

        String response = mockMvc.perform(post("/api/habits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(habit)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Habit created = objectMapper.readValue(response, Habit.class);

        created.setName("Hydration");
        created.setDescription("Drink 8 glasses");

        mockMvc.perform(put("/api/habits/" + created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(created)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Hydration"))
                .andExpect(jsonPath("$.description").value("Drink 8 glasses"));
    }

    @Test
    @DisplayName("Should delete habit")
    void testDeleteHabit() throws Exception {
        Habit habit = new Habit("Delete Me", "Temporary habit", Habit.Frequency.DAILY, 7);

        String response = mockMvc.perform(post("/api/habits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(habit)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Habit created = objectMapper.readValue(response, Habit.class);

        mockMvc.perform(delete("/api/habits/" + created.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/habits/" + created.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should get habits by frequency")
    void testGetHabitsByFrequency() throws Exception {
        Habit h1 = new Habit("Gym", "Go to gym", Habit.Frequency.DAILY, 7);
        Habit h2 = new Habit("Review", "Weekly review", Habit.Frequency.WEEKLY, 1);

        mockMvc.perform(post("/api/habits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(h1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/habits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(h2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/habits/frequency/DAILY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", contains("Gym")));
    }
}
