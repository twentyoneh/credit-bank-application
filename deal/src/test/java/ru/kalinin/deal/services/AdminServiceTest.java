// java
package ru.kalinin.deal.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import ru.kalinin.deal.dto.StatementDto;
import ru.kalinin.deal.models.Statement;
import ru.kalinin.deal.models.StatusHistory;
import ru.kalinin.deal.models.enums.ChangeType;
import ru.kalinin.deal.models.enums.Status;
import ru.kalinin.deal.repositories.StatementRepository;
import ru.kalinin.deal.util.StatementMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private StatementRepository statementRepository;

    @Mock
    private StatementMapper statementMapper;

    @InjectMocks
    private AdminService adminService;

    @Test
    void findAllStatements_returnsMappedList() {
        Statement s1 = new Statement();
        s1.setId(UUID.randomUUID());
        s1.setStatusHistory(new ArrayList<>());

        Statement s2 = new Statement();
        s2.setId(UUID.randomUUID());
        s2.setStatusHistory(new ArrayList<>());

        when(statementRepository.findAll()).thenReturn(List.of(s1, s2));

        StatementDto d1 = new StatementDto();
        StatementDto d2 = new StatementDto();
        when(statementMapper.toStatementDto(s1)).thenReturn(d1);
        when(statementMapper.toStatementDto(s2)).thenReturn(d2);

        ResponseEntity<List<StatementDto>> resp = adminService.findAllStatements();

        assertEquals(200, resp.getStatusCodeValue());
        assertNotNull(resp.getBody());
        assertEquals(2, resp.getBody().size());
        assertSame(d1, resp.getBody().get(0));
        assertSame(d2, resp.getBody().get(1));

        verify(statementRepository).findAll();
        verify(statementMapper).toStatementDto(s1);
        verify(statementMapper).toStatementDto(s2);
    }

    @Test
    void findStatementById_success_returnsDto() {
        UUID id = UUID.randomUUID();

        Statement s = new Statement();
        s.setId(id);
        s.setStatusHistory(new ArrayList<>());

        when(statementRepository.findById(id)).thenReturn(Optional.of(s));

        StatementDto dto = new StatementDto();
        when(statementMapper.toStatementDto(s)).thenReturn(dto);

        ResponseEntity<StatementDto> resp = adminService.findStatementById(id.toString());

        assertEquals(200, resp.getStatusCodeValue());
        assertSame(dto, resp.getBody());

        verify(statementRepository).findById(id);
        verify(statementMapper).toStatementDto(s);
    }

    @Test
    void findStatementById_notFound_returns204NoContent() {
        UUID id = UUID.randomUUID();
        when(statementRepository.findById(id)).thenReturn(Optional.empty());

        ResponseEntity<StatementDto> resp = adminService.findStatementById(id.toString());

        assertEquals(204, resp.getStatusCodeValue());
        assertNull(resp.getBody());
        verify(statementRepository).findById(id);
        verifyNoInteractions(statementMapper);
    }

    @Test
    void saveStatementStatus_updatesStatusAndAppendsHistory() {
        Statement s = new Statement();
        s.setId(UUID.randomUUID());
        s.setStatusHistory(new ArrayList<>());

        assertEquals(0, s.getStatusHistory().size());

        adminService.saveStatementStatus(s, Status.APPROVED, ChangeType.AUTOMATIC);

        assertEquals(Status.APPROVED, s.getStatus());
        assertEquals(1, s.getStatusHistory().size());
        StatusHistory last = s.getStatusHistory().get(0);
        assertEquals(Status.APPROVED, last.getStatus());
        assertEquals(ChangeType.AUTOMATIC, last.getChangeType());
        assertNotNull(last.getTime());
        verifyNoInteractions(statementRepository, statementMapper);
    }

    @Test
    void saveStatementStatus_byId_findsStatementAndUpdates() {
        UUID id = UUID.randomUUID();

        Statement s = new Statement();
        s.setId(id);
        s.setStatusHistory(new ArrayList<>());

        when(statementRepository.findById(id)).thenReturn(Optional.of(s));

        adminService.saveStatementStatus(id.toString(), Status.CC_APPROVED, ChangeType.AUTOMATIC);

        assertEquals(Status.CC_APPROVED, s.getStatus());
        assertEquals(1, s.getStatusHistory().size());
        StatusHistory last = s.getStatusHistory().get(0);
        assertEquals(Status.CC_APPROVED, last.getStatus());
        assertEquals(ChangeType.AUTOMATIC, last.getChangeType());
        assertNotNull(last.getTime());

        verify(statementRepository).findById(id);
        verifyNoInteractions(statementMapper);
    }
}