from django.db.models import QuerySet


def apply_exact_query_filters(
    queryset: QuerySet,
    query_params,
    filter_map: dict[str, str],
    integer_params: set[str] | None = None,
) -> QuerySet:
    integer_params = integer_params or set()

    for param_name, lookup in filter_map.items():
        value = query_params.get(param_name)
        if value in (None, ""):
            continue
        if param_name in integer_params:
            try:
                value = int(value)
            except (TypeError, ValueError):
                return queryset.none()
        queryset = queryset.filter(**{lookup: value})

    return queryset

