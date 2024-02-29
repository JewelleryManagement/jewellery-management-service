package jewellery.inventory.unit.aspect;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import jewellery.inventory.aspect.EventAspect;
import jewellery.inventory.aspect.annotation.LogDeleteEvent;
import jewellery.inventory.aspect.annotation.LogUpdateEvent;
import jewellery.inventory.service.SystemEventService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EventAspectTest {
  @InjectMocks private EventAspect eventAspect;

  @Mock private SystemEventService systemEventServiceMock;
  @Mock private ProceedingJoinPoint proceedingJoinPointMock;
  @Mock private LogUpdateEvent logUpdateEventMock;
  @Mock private LogDeleteEvent logDeletEventMock;

  @Test
  void logUpdateWillFailWhenServiceNotFetcher() throws Throwable {

    assertNull(eventAspect.logUpdate(proceedingJoinPointMock, logUpdateEventMock));
  }

  @Test
  void logDeleteWillFailWhenServiceNotFetcher() {
    when(proceedingJoinPointMock.getTarget()).thenReturn(new Object());

    eventAspect.logDeletion(proceedingJoinPointMock, logDeletEventMock);

    verify(systemEventServiceMock, times(0)).logEvent(any(), any());
  }
}
