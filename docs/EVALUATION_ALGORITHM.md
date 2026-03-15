# Evaluation Scoring Algorithm

## Scope
Applies to evaluation records handled by `EvaluationService` in the attendance module.

- Weighted score calculation: `recalculateScoreAndGrade()`
- Grade mapping: `mapGrade()`
- Workflow guards: `submit()`, `review()`, `finalizeEvaluation()`

## Inputs
For each evaluation template criterion:
- `weight` (BigDecimal)
- `maxScore` (Integer)

For each submitted criterion score:
- `score` (BigDecimal)

## Formula
For every criterion with valid `maxScore > 0` and a provided score:

$$
normalizedPercent_i = \frac{score_i \times 100}{maxScore_i}
$$

$$
weightedSum = \sum_i normalizedPercent_i \times weight_i
$$

$$
totalWeight = \sum_i weight_i
$$

Final score:

$$
totalScore = \frac{weightedSum}{totalWeight}
$$

Implementation details:
- Uses `HALF_UP` rounding.
- Internal normalization precision: 6 decimals.
- Final `totalScore`: 2 decimals.
- If there are no valid weighted criteria, both `totalScore` and `grade` are `null`.

## Grade Mapping
- `A`: score >= 90
- `B`: 80 <= score < 90
- `C`: 70 <= score < 80
- `D`: 60 <= score < 70
- `F`: score < 60

## Validation Rules
- Score input must satisfy `0 <= score <= maxScore`.
- Criterion must belong to the selected template.
- Evaluation type must match template type.
- Duplicate evaluation is blocked by `(internProfileId, evaluationType, periodStart, periodEnd)`.

## Workflow Preconditions
- `DRAFT -> SUBMITTED`: requires calculated `totalScore` and `grade`.
- `SUBMITTED -> REVIEWED`: only from `SUBMITTED`.
- `REVIEWED -> FINALIZED`: only from `REVIEWED`.

## Practical Verification (March 15, 2026)
End-to-end smoke run passed on attendance service:
1. Create evaluation
2. Update criterion scores
3. Submit
4. Review
5. Finalize

Observed output example:
- `status = FINALIZED`
- `grade = B`
- `totalScore = 80.0`
