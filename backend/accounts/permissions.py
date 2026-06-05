from rest_framework import permissions


class IsOwner(permissions.BasePermission):
    """
    Object permission for models that store their student owner in an owner field.
    """

    def has_object_permission(self, request, view, obj) -> bool:
        owner = getattr(obj, "owner", None)
        if owner is None:
            owner = getattr(obj, "user", None)
        return owner == request.user

