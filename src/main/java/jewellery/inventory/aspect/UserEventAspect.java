package jewellery.inventory.aspect;

// import java.util.UUID;
// import jewellery.inventory.dto.request.UserRequestDto;
// import jewellery.inventory.dto.response.UserResponseDto;
// import jewellery.inventory.mapper.UserMapper;
// import jewellery.inventory.repository.SystemEventRepository;
// import jewellery.inventory.repository.UserRepository;
// import lombok.RequiredArgsConstructor;
// import org.aspectj.lang.ProceedingJoinPoint;
// import org.aspectj.lang.annotation.AfterReturning;
// import org.aspectj.lang.annotation.Around;
// import org.aspectj.lang.annotation.Aspect;
// import org.aspectj.lang.annotation.Pointcut;
// import org.springframework.stereotype.Component;

// @Aspect
// @Component
// @RequiredArgsConstructor
// public class UserEventAspect {
//  private final UserEventService userEventService;
//  private final SystemEventRepository systemEventRepository;
//  private final UserRepository userRepository;
//  private final UserMapper userMapper;
//
//  @Pointcut(
//      "execution(*
// jewellery.inventory.service.UserService.createUser(jewellery.inventory.dto.request.UserRequestDto))")
//  public void userCreationMethods() {}
//
//  @AfterReturning(pointcut = "userCreationMethods()", returning = "createdUser")
//  public void logUserCreation(UserResponseDto createdUser) {
//    userEventService.logUserCreation(createdUser);
//  }
//
//  @Pointcut("execution(* jewellery.inventory.service.UserService.deleteUser(java.util.UUID))")
//  public void userDeletionMethods() {}
//
//  @Around("userDeletionMethods()")
//  public void logUserDeletion(ProceedingJoinPoint  joinPoint) throws Throwable {
//    UUID userId = (UUID) joinPoint.getArgs()[0];
//    userEventService.logUserDeletion(joinPoint, userId);
//    joinPoint.proceed();
//  }
//
//  @Pointcut(
//      "execution(*
// jewellery.inventory.service.UserService.updateUser(jewellery.inventory.dto.request.UserRequestDto, java.util.UUID)) && args(userRequest, id)")
//  public void userUpdateMethods(UserRequestDto userRequest, UUID id) {}
//
//  @Around("userUpdateMethods(userRequest, id)")
//  public UserResponseDto logUserUpdate(
//      ProceedingJoinPoint joinPoint, UserRequestDto userRequest, UUID id) throws Throwable {
//    return userEventService.logUserUpdate(joinPoint, userRequest, id);
//  }
// }
