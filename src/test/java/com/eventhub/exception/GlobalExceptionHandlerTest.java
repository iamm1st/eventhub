package com.eventhub.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.lang.reflect.Method;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleBadRequestShouldReturn400() {
        MockHttpServletRequest request = request("/api/test");

        ResponseEntity<ErrorResponse> response = handler.handleBadRequest(new BadRequestException("Bad request message"), request);

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals("Bad request message", response.getBody().getMessage());
        assertEquals("/api/test", response.getBody().getPath());
    }

    @Test
    void handleNotFoundShouldReturn404() {
        MockHttpServletRequest request = request("/api/test/1");

        ResponseEntity<ErrorResponse> response = handler.handleNotFound(new ResourceNotFoundException("Resource not found"), request);

        assertEquals(404, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Not Found", response.getBody().getError());
        assertEquals("Resource not found", response.getBody().getMessage());
        assertEquals("/api/test/1", response.getBody().getPath());
    }

    @Test
    void handleConflictShouldReturn409() {
        MockHttpServletRequest request = request("/api/test");

        ResponseEntity<ErrorResponse> response = handler.handleConflict(new ConflictException("Conflict message"), request);

        assertEquals(409, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Conflict", response.getBody().getError());
        assertEquals("Conflict message", response.getBody().getMessage());
    }

    @Test
    void handleForbiddenShouldReturn403ForForbiddenActionException() {
        MockHttpServletRequest request = request("/api/admin/users");

        ResponseEntity<ErrorResponse> response = handler.handleForbidden(new ForbiddenActionException("Forbidden action"), request);

        assertEquals(403, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Forbidden", response.getBody().getError());
        assertEquals("Forbidden action", response.getBody().getMessage());
    }

    @Test
    void handleForbiddenShouldReturn403ForAccessDeniedException() {
        MockHttpServletRequest request = request("/api/admin/users");

        ResponseEntity<ErrorResponse> response = handler.handleForbidden(new AccessDeniedException("Access denied"), request);

        assertEquals(403, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Forbidden", response.getBody().getError());
        assertEquals("Access denied", response.getBody().getMessage());
    }

    @Test
    void handleForbiddenShouldReturn403ForDisabledException() {
        MockHttpServletRequest request = request("/api/admin/users");

        ResponseEntity<ErrorResponse> response = handler.handleForbidden(new DisabledException("User is disabled"), request);

        assertEquals(403, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Forbidden", response.getBody().getError());
        assertEquals("User is disabled", response.getBody().getMessage());
    }

    @Test
    void handleUnauthorizedShouldReturn401() {
        MockHttpServletRequest request = request("/api/auth/login");

        ResponseEntity<ErrorResponse> response = handler.handleUnauthorized(request);

        assertEquals(401, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Unauthorized", response.getBody().getError());
        assertEquals("Invalid email or password", response.getBody().getMessage());
        assertEquals("/api/auth/login", response.getBody().getPath());
    }

    @Test
    void handleValidationShouldReturn400WithFieldErrors() throws NoSuchMethodException {
        MockHttpServletRequest request = request("/api/auth/register");

        RegisterValidationTarget target = new RegisterValidationTarget();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "registerValidationTarget");
        bindingResult.addError(new FieldError("registerValidationTarget", "email",
                "must be a well-formed email address"));
        bindingResult.addError(new FieldError("registerValidationTarget", "password", "size must be between 6 and 100"));

        MethodParameter methodParameter = methodParameter("register", RegisterValidationTarget.class);

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);
        ResponseEntity<ValidationErrorResponse> response = handler.handleValidation(exception, request);

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals("Validation failed", response.getBody().getMessage());
        assertEquals("must be a well-formed email address", response.getBody().getErrors().get("email"));
        assertEquals("size must be between 6 and 100", response.getBody().getErrors().get("password"));
    }

    @Test
    void handleConstraintViolationShouldReturn400WithErrors() {
        MockHttpServletRequest request = request("/api/events");

        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);

        when(path.toString()).thenReturn("page");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must be greater than or equal to 0");

        ConstraintViolationException exception = new ConstraintViolationException(Set.of(violation));

        ResponseEntity<ValidationErrorResponse> response = handler.handleConstraintViolation(exception, request);

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Validation failed", response.getBody().getMessage());
        assertEquals("must be greater than or equal to 0", response.getBody().getErrors().get("page"));
    }

    @Test
    void handleTypeMismatchShouldReturn400() throws NoSuchMethodException {
        MockHttpServletRequest request = request("/api/events/abc");

        MethodParameter methodParameter = methodParameter("getEventById", Long.class);

        MethodArgumentTypeMismatchException exception =
                new MethodArgumentTypeMismatchException("abc", Long.class, "id", methodParameter, new NumberFormatException("Invalid number"));

        ResponseEntity<ErrorResponse> response = handler.handleTypeMismatch(exception, request);

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Invalid value for parameter: id", response.getBody().getMessage());
    }

    @Test
    void handleUnreadableMessageShouldReturn400() {
        MockHttpServletRequest request = request("/api/categories");

        ResponseEntity<ErrorResponse> response = handler.handleUnreadableMessage(request);

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Request body is missing or malformed", response.getBody().getMessage());
    }

    @Test
    void handleUnsupportedMediaTypeShouldReturn415() {
        MockHttpServletRequest request = request("/api/categories");

        ResponseEntity<ErrorResponse> response = handler.handleUnsupportedMediaType(request);

        assertEquals(415, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Unsupported Media Type", response.getBody().getError());
        assertEquals("Unsupported media type. Use application/json", response.getBody().getMessage());
    }

    @Test
    void handleUnexpectedShouldReturn500() {
        MockHttpServletRequest request = request("/api/test");

        ResponseEntity<ErrorResponse> response = handler.handleUnexpected(new RuntimeException("Unexpected"), request);

        assertEquals(500, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Internal Server Error", response.getBody().getError());
        assertEquals("Unexpected server error", response.getBody().getMessage());
    }

    @Test
    void handleUnauthorizedShouldIgnoreExceptionMessage() {
        MockHttpServletRequest request = request("/api/auth/login");

        ResponseEntity<ErrorResponse> response = handler.handleUnauthorized(request);

        assertEquals(401, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Invalid email or password", response.getBody().getMessage());
    }

    private MockHttpServletRequest request(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(uri);
        return request;
    }

    private MethodParameter methodParameter(String methodName, Class<?> parameterType) throws NoSuchMethodException {
        Method method = TestController.class.getDeclaredMethod(methodName, parameterType);
        return new MethodParameter(method, 0);
    }

    @SuppressWarnings("unused")
    private static class TestController {

        public void register(RegisterValidationTarget target) {
        }

        public void getEventById(Long id) {
        }
    }

    private static class RegisterValidationTarget {
    }
}