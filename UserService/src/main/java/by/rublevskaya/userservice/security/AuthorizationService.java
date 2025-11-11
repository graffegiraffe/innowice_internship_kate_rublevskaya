package by.rublevskaya.userservice.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthorizationService {

    public UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            return (UserPrincipal) authentication.getPrincipal();
        }
        throw new AccessDeniedException("User not authenticated");
    }

    public void checkUserAccess(Long resourceUserId) {
        UserPrincipal currentUser = getCurrentUser();

        if (currentUser.isAdmin()) {
            log.debug("Admin access granted for user: {}", currentUser.getLogin());
            return;
        }

        if (!currentUser.getUserId().equals(resourceUserId)) {
            log.warn("Access denied for user {} trying to access resource of user {}",
                    currentUser.getUserId(), resourceUserId);
            throw new AccessDeniedException("Access denied: You can only access your own resources");
        }

        log.debug("User access granted for user: {}", currentUser.getLogin());
    }

    public boolean isAdmin() {
        try {
            return getCurrentUser().isAdmin();
        } catch (Exception e) {
            return false;
        }
    }
}