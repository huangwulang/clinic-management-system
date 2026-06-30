const ISO_DATE_TIME_PATTERN =
  /^(\d{4}-\d{2}-\d{2})T(\d{2}:\d{2}:\d{2})(?:\.\d+)?(?:Z|[+-]\d{2}:\d{2})?$/;

export const formatDateTime = (value: string): string => {
  const match = value.match(ISO_DATE_TIME_PATTERN);
  return match ? `${match[1]} ${match[2]}` : value;
};

export const normalizeDateTimeStrings = <T>(value: T): T => {
  if (typeof value === "string") {
    return formatDateTime(value) as T;
  }

  if (Array.isArray(value)) {
    return value.map((item) => normalizeDateTimeStrings(item)) as T;
  }

  if (value && typeof value === "object" && Object.getPrototypeOf(value) === Object.prototype) {
    return Object.fromEntries(
      Object.entries(value).map(([key, item]) => [key, normalizeDateTimeStrings(item)]),
    ) as T;
  }

  return value;
};
