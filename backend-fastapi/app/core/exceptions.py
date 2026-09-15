class EntityNotFoundException(Exception):
    pass


class DuplicateResourceException(Exception):
    pass


class InvalidOperationException(Exception):
    pass


class UnauthorizedException(Exception):
    pass


class ForbiddenException(Exception):
    """Mirrors the Java code's use of SecurityException for authorization failures."""
