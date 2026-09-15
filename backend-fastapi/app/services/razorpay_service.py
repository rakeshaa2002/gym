import base64
import hashlib
import hmac
import re

import httpx

from app.core.config import settings

_ORDER_ID_RE = re.compile(r'"id"\s*:\s*"(order_[^"]+)"')


def is_configured() -> bool:
    return bool(settings.razorpay_key_id) and bool(settings.razorpay_key_secret)


def create_order(amount_paise: int, currency: str, receipt: str) -> str:
    if not is_configured():
        raise RuntimeError("Razorpay is not configured on the server")
    auth = base64.b64encode(f"{settings.razorpay_key_id}:{settings.razorpay_key_secret}".encode()).decode()
    body = {"amount": amount_paise, "currency": currency, "receipt": receipt, "payment_capture": 1}
    resp = httpx.post(
        "https://api.razorpay.com/v1/orders",
        headers={"Authorization": f"Basic {auth}", "Content-Type": "application/json"},
        json=body,
        timeout=15,
    )
    match = _ORDER_ID_RE.search(resp.text)
    if not match:
        raise RuntimeError(f"Razorpay order creation failed: {resp.text}")
    return match.group(1)


def verify_signature(order_id: str, payment_id: str, signature: str) -> bool:
    if not is_configured() or not order_id or not payment_id or not signature:
        return False
    payload = f"{order_id}|{payment_id}".encode()
    expected = hmac.new(settings.razorpay_key_secret.encode(), payload, hashlib.sha256).hexdigest()
    return hmac.compare_digest(expected, signature)
