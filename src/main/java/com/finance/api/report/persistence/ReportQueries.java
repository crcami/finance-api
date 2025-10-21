package com.finance.api.report.persistence;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class ReportQueries {

  @PersistenceContext
  private EntityManager em;

  public SummaryRow querySummary(UUID userId, LocalDate from, LocalDate to, List<UUID> categoryIds) {
    String sql =
        """
        select
          coalesce(sum(case when r.kind='INCOME'  then r.amount else 0 end),0) as income,
          coalesce(sum(case when r.kind='EXPENSE' then r.amount else 0 end),0) as expense
        from record r
        where r.user_id = :userId
          and r.status <> 'CANCELED'
          and r.due_date between :from and :to
          and (:catsEmpty or r.category_id = any(:cats))
        """;

    var q = em.createNativeQuery(sql, "summary-mapping");
    q.setParameter("userId", userId);
    q.setParameter("from", Date.valueOf(from));
    q.setParameter("to", Date.valueOf(to));
    q.setParameter("catsEmpty", categoryIds == null || categoryIds.isEmpty());
    q.setParameter("cats", categoryIds == null || categoryIds.isEmpty() ? new UUID[]{} : categoryIds.toArray(UUID[]::new));
    Object[] row = (Object[]) q.getSingleResult();
    return new SummaryRow((BigDecimal) row[0], (BigDecimal) row[1]);
  }

  public List<CashflowRow> queryCashflow(UUID userId, LocalDate from, LocalDate to, List<UUID> categoryIds) {
    String sql =
        """
        select date_trunc('month', r.due_date) as m,
               coalesce(sum(case when r.kind='INCOME'  then r.amount else 0 end),0) as income,
               coalesce(sum(case when r.kind='EXPENSE' then r.amount else 0 end),0) as expense
        from record r
        where r.user_id = :userId
          and r.status <> 'CANCELED'
          and r.due_date between :from and :to
          and (:catsEmpty or r.category_id = any(:cats))
        group by 1
        order by 1
        """;

    var q = em.createNativeQuery(sql);
    q.setParameter("userId", userId);
    q.setParameter("from", Date.valueOf(from));
    q.setParameter("to", Date.valueOf(to));
    q.setParameter("catsEmpty", categoryIds == null || categoryIds.isEmpty());
    q.setParameter("cats", categoryIds == null || categoryIds.isEmpty() ? new UUID[]{} : categoryIds.toArray(UUID[]::new));

    List<Object[]> rows = q.getResultList();
    List<CashflowRow> out = new ArrayList<>();
    for (Object[] r : rows) {
      java.sql.Timestamp ts = (java.sql.Timestamp) r[0];
      YearMonth ym = YearMonth.from(ts.toLocalDateTime().toLocalDate());
      out.add(new CashflowRow(ym, (BigDecimal) r[1], (BigDecimal) r[2]));
    }
    return out;
  }

  public List<CategoryRow> queryByCategory(UUID userId, LocalDate from, LocalDate to, List<UUID> categoryIds) {
    String sql =
        """
        select r.category_id as id,
               coalesce(c.name,'Uncategorized') as name,
               coalesce(sum(case when r.kind='INCOME'  then r.amount else 0 end),0) as income,
               coalesce(sum(case when r.kind='EXPENSE' then r.amount else 0 end),0) as expense
        from record r
        left join category c on c.id = r.category_id
        where r.user_id = :userId
          and r.status <> 'CANCELED'
          and r.due_date between :from and :to
          and (:catsEmpty or r.category_id = any(:cats))
        group by r.category_id, c.name
        order by name
        """;

    var q = em.createNativeQuery(sql);
    q.setParameter("userId", userId);
    q.setParameter("from", Date.valueOf(from));
    q.setParameter("to", Date.valueOf(to));
    q.setParameter("catsEmpty", categoryIds == null || categoryIds.isEmpty());
    q.setParameter("cats", categoryIds == null || categoryIds.isEmpty() ? new UUID[]{} : categoryIds.toArray(UUID[]::new));

    List<Object[]> rows = q.getResultList();
    List<CategoryRow> out = new ArrayList<>();
    for (Object[] r : rows) {
      UUID id = (UUID) r[0];            
      String name = (String) r[1];
      out.add(new CategoryRow(id, name, (BigDecimal) r[2], (BigDecimal) r[3]));
    }
    return out;
  }


  public record SummaryRow(BigDecimal income, BigDecimal expense) { }
  public record CashflowRow(YearMonth month, BigDecimal income, BigDecimal expense) { }
  public record CategoryRow(UUID id, String name, BigDecimal income, BigDecimal expense) { }
}
