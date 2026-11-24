package pathfinder.api;

/** A problem the user can fix; reported as HTTP 400 with this message. */
final class InvalidRequestException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    InvalidRequestException(String message) {
        super(message);
    }
}