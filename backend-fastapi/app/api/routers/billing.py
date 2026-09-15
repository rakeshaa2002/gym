from fastapi import APIRouter, Depends
from sqlalchemy import select
from sqlalchemy.orm import Session

from app.api.deps import get_db
from app.models.membership import Transaction
from app.schemas.common import success

router = APIRouter(prefix="/api/billing", tags=["billing"])


@router.get("/transactions")
def list_transactions(db: Session = Depends(get_db)):
    rows = db.scalars(select(Transaction).order_by(Transaction.transaction_date.desc()))
    data = [
        {
            "id": t.id, "memberId": t.member_id, "planId": t.plan_id, "amount": t.amount, "months": t.months,
            "paymentMethod": t.payment_method, "status": t.status, "transactionDate": t.transaction_date.isoformat(),
            "razorpayOrderId": t.razorpay_order_id, "razorpayPaymentId": t.razorpay_payment_id,
        }
        for t in rows
    ]
    return success(data, "Transactions retrieved successfully")
