export const COUNTRY_CODE_OPTIONS = [
  { label: "India (+91)", value: "+91", maxLength: 10 },
  { label: "United States (+1)", value: "+1", maxLength: 10 },
  { label: "United Kingdom (+44)", value: "+44", maxLength: 10 },
  { label: "Australia (+61)", value: "+61", maxLength: 9 },
  { label: "Canada (+1)", value: "+1", maxLength: 10 },
];

export function ensureCountryCodeValue(value) {
  const match = COUNTRY_CODE_OPTIONS.find((opt) => opt.value === value);
  return match ? match.value : "+91";
}

export function getCountryOptionByValue(value) {
  return COUNTRY_CODE_OPTIONS.find((opt) => opt.value === value) || COUNTRY_CODE_OPTIONS[0];
}

export function getCountryAllowedLengths(value) {
  const option = getCountryOptionByValue(value);
  return option?.maxLength ? [option.maxLength] : [10];
}

export function getCountryDisplayMaxLength(value) {
  const option = getCountryOptionByValue(value);
  return option?.maxLength || 10;
}

export function sanitizePhoneDigits(value, maxLength, allowedLengths = []) {
  const digits = String(value || "").replace(/\D/g, "");
  const length = maxLength || (allowedLengths.length ? Math.max(...allowedLengths) : digits.length);
  return digits.slice(0, length);
}

export function validatePhoneNumber(value, countryCode) {
  const digits = sanitizePhoneDigits(value);
  if (!digits) {
    return "Phone number is required";
  }

  const allowed = getCountryAllowedLengths(countryCode);
  if (!allowed.includes(digits.length)) {
    return `Phone number must be ${allowed.join(" or ")} digits long`;
  }

  return null;
}
