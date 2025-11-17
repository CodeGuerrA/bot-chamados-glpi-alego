package com.chatbot.chatbotglpi.conversation;

import com.chatbot.chatbotglpi.conversation.domain.entity.ConversationState;
import com.chatbot.chatbotglpi.conversation.domain.enums.StateEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe ConversationState.
 *
 * Esta classe testa as funcionalidades básicas do estado da conversa:
 * - Adição e recuperação de dados
 * - Verificação de completude
 * - Atualização de atividade
 */
@DisplayName("Testes do ConversationState")
class ConversationStateTest {

    private ConversationState conversationState;

    /**
     * Setup executado antes de cada teste.
     * Cria uma instância nova de ConversationState.
     */
    @BeforeEach
    void setUp() {
        conversationState = ConversationState.builder()
                .phone("5511999999999")
                .currentState(StateEnum.GREETING)
                .startedAt(LocalDateTime.now())
                .build();
    }

    // =========================================================================
    // TESTES DO MÉTODO addData()
    // =========================================================================

    @Test
    @DisplayName("Deve adicionar dados corretamente")
    void testAddData_AddsDataCorrectly() {
        conversationState.addData("title", "Problema com impressora");

        assertEquals("Problema com impressora", conversationState.getData("title"));
    }

    @Test
    @DisplayName("Deve sobrescrever dados existentes")
    void testAddData_OverwritesExistingData() {
        conversationState.addData("title", "Título antigo");
        conversationState.addData("title", "Título novo");

        assertEquals("Título novo", conversationState.getData("title"));
    }

    @Test
    @DisplayName("Deve criar map de dados quando null")
    void testAddData_CreatesMapWhenNull() {
        // Garante que data está null
        conversationState = ConversationState.builder().build();

        conversationState.addData("key", "value");

        assertNotNull(conversationState.getData("key"));
        assertEquals("value", conversationState.getData("key"));
    }

    // =========================================================================
    // TESTES DO MÉTODO getData()
    // =========================================================================

    @Test
    @DisplayName("Deve retornar dados existentes")
    void testGetData_ReturnsExistingData() {
        conversationState.addData("locate", "Sala 101");

        assertEquals("Sala 101", conversationState.getData("locate"));
    }

    @Test
    @DisplayName("Deve retornar null para dados inexistentes")
    void testGetData_ReturnsNullForNonExistentData() {
        String result = conversationState.getData("inexistente");

        assertNull(result);
    }

    @Test
    @DisplayName("Deve retornar null quando map é null")
    void testGetData_ReturnsNullWhenMapIsNull() {
        conversationState = ConversationState.builder().build();

        String result = conversationState.getData("any");

        assertNull(result);
    }

    // =========================================================================
    // TESTES DO MÉTODO hasData()
    // =========================================================================

    @Test
    @DisplayName("Deve retornar true quando dado existe")
    void testHasData_ReturnsTrueWhenDataExists() {
        conversationState.addData("ramal", "1234");

        assertTrue(conversationState.hasData("ramal"));
    }

    @Test
    @DisplayName("Deve retornar false quando dado não existe")
    void testHasData_ReturnsFalseWhenDataDoesNotExist() {
        assertFalse(conversationState.hasData("inexistente"));
    }

    @Test
    @DisplayName("Deve retornar false quando map é null")
    void testHasData_ReturnsFalseWhenMapIsNull() {
        conversationState = ConversationState.builder().build();

        assertFalse(conversationState.hasData("any"));
    }

    // =========================================================================
    // TESTES DO MÉTODO isComplete()
    // =========================================================================

    @Test
    @DisplayName("Deve retornar true quando todos os dados obrigatórios existem")
    void testIsComplete_ReturnsTrueWhenAllDataExists() {
        conversationState.addData("title", "Título");
        conversationState.addData("description", "Descrição");
        conversationState.addData("locate", "Sala 101");
        conversationState.addData("ramal", "1234");

        assertTrue(conversationState.isComplete());
    }

    @Test
    @DisplayName("Deve retornar false quando falta título")
    void testIsComplete_ReturnsFalseWhenMissingTitle() {
        conversationState.addData("description", "Descrição");
        conversationState.addData("locate", "Sala 101");
        conversationState.addData("ramal", "1234");

        assertFalse(conversationState.isComplete());
    }

    @Test
    @DisplayName("Deve retornar false quando falta descrição")
    void testIsComplete_ReturnsFalseWhenMissingDescription() {
        conversationState.addData("title", "Título");
        conversationState.addData("locate", "Sala 101");
        conversationState.addData("ramal", "1234");

        assertFalse(conversationState.isComplete());
    }

    @Test
    @DisplayName("Deve retornar false quando falta local")
    void testIsComplete_ReturnsFalseWhenMissingLocate() {
        conversationState.addData("title", "Título");
        conversationState.addData("description", "Descrição");
        conversationState.addData("ramal", "1234");

        assertFalse(conversationState.isComplete());
    }

    @Test
    @DisplayName("Deve retornar false quando falta ramal")
    void testIsComplete_ReturnsFalseWhenMissingRamal() {
        conversationState.addData("title", "Título");
        conversationState.addData("description", "Descrição");
        conversationState.addData("locate", "Sala 101");

        assertFalse(conversationState.isComplete());
    }

    @Test
    @DisplayName("Deve retornar false quando nenhum dado existe")
    void testIsComplete_ReturnsFalseWhenNoDataExists() {
        assertFalse(conversationState.isComplete());
    }

    // =========================================================================
    // TESTES DO MÉTODO updateActivity()
    // =========================================================================

    @Test
    @DisplayName("Deve atualizar lastActivity para data/hora atual")
    void testUpdateActivity_UpdatesLastActivityToNow() {
        LocalDateTime before = LocalDateTime.now();

        conversationState.updateActivity();

        LocalDateTime after = LocalDateTime.now();
        LocalDateTime lastActivity = conversationState.getLastActivity();

        assertNotNull(lastActivity);
        assertTrue(lastActivity.isAfter(before) || lastActivity.isEqual(before));
        assertTrue(lastActivity.isBefore(after) || lastActivity.isEqual(after));
    }

    @Test
    @DisplayName("Deve sobrescrever lastActivity anterior")
    void testUpdateActivity_OverwritesPreviousActivity() throws InterruptedException {
        conversationState.updateActivity();
        LocalDateTime firstUpdate = conversationState.getLastActivity();

        // Aguarda 1ms para garantir diferença de tempo
        Thread.sleep(1);

        conversationState.updateActivity();
        LocalDateTime secondUpdate = conversationState.getLastActivity();

        assertNotNull(firstUpdate);
        assertNotNull(secondUpdate);
        assertTrue(secondUpdate.isAfter(firstUpdate));
    }

    // =========================================================================
    // TESTES DO BUILDER
    // =========================================================================

    @Test
    @DisplayName("Builder deve criar ConversationState corretamente")
    void testBuilder_CreatesConversationStateCorrectly() {
        LocalDateTime now = LocalDateTime.now();

        ConversationState state = ConversationState.builder()
                .phone("5511999999999")
                .currentState(StateEnum.COLLECTING_DESCRIPTION)
                .startedAt(now)
                .lastActivity(now)
                .build();

        assertEquals("5511999999999", state.getPhone());
        assertEquals(StateEnum.COLLECTING_DESCRIPTION, state.getCurrentState());
        assertEquals(now, state.getStartedAt());
        assertEquals(now, state.getLastActivity());
    }
}
