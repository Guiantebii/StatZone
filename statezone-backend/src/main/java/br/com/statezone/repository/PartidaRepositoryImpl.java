package br.com.statezone.repository;

import br.com.statezone.enums.StatusCampeonato;
import br.com.statezone.enums.StatusPartida;
import br.com.statezone.model.Partida;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

public class PartidaRepositoryImpl implements PartidaRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Partida> buscarFiltradas(StatusPartida status, Long campeonatoId, boolean incluirRascunho, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Partida> query = cb.createQuery(Partida.class);
        Root<Partida> root = query.from(Partida.class);
        root.fetch("timeMandante", JoinType.INNER);
        root.fetch("timeVisitante", JoinType.INNER);
        root.fetch("campeonato", JoinType.INNER);
        query.select(root).distinct(true);
        query.where(filtros(cb, root, status, campeonatoId, incluirRascunho));
        query.orderBy(ordenacao(cb, root, pageable.getSort()));

        TypedQuery<Partida> typed = entityManager.createQuery(query);
        typed.setFirstResult((int) pageable.getOffset());
        typed.setMaxResults(pageable.getPageSize());
        List<Partida> content = typed.getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Partida> countRoot = countQuery.from(Partida.class);
        countQuery.select(cb.count(countRoot));
        countQuery.where(filtros(cb, countRoot, status, campeonatoId, incluirRascunho));
        long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }

    private Predicate[] filtros(CriteriaBuilder cb, Root<Partida> root, StatusPartida status, Long campeonatoId, boolean incluirRascunho) {
        List<Predicate> predicates = new ArrayList<>();
        if (status != null) {
            predicates.add(cb.equal(root.get("status"), status));
        }
        if (campeonatoId != null) {
            predicates.add(cb.equal(root.get("campeonato").get("id"), campeonatoId));
        }
        if (!incluirRascunho) {
            predicates.add(cb.notEqual(root.get("campeonato").get("status"), StatusCampeonato.RASCUNHO));
        }
        return predicates.toArray(Predicate[]::new);
    }

    private List<Order> ordenacao(CriteriaBuilder cb, Root<Partida> root, Sort sort) {
        List<Order> orders = new ArrayList<>();
        if (sort == null || sort.isUnsorted()) {
            orders.add(cb.desc(root.get("criadoEm")));
            orders.add(cb.desc(root.get("id")));
            return orders;
        }
        for (Sort.Order order : sort) {
            orders.add(order.isAscending() ? cb.asc(root.get(order.getProperty())) : cb.desc(root.get(order.getProperty())));
        }
        return orders;
    }
}
