package com.moekr.moocoder.data.dao;

import com.moekr.moocoder.data.entity.Exam;
import com.moekr.moocoder.data.entity.User;
import com.moekr.moocoder.util.enums.ExamStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ExamDAO extends JpaRepository<Exam, Integer> {
	Exam findById(int id);

	List<Exam> findAllByStatus(ExamStatus status);

	Page<Exam> findAllByStatus(ExamStatus status, Pageable pageable);

	default Page<Exam> findAllByActualStatus(ExamStatus status, Pageable pageable) {
		switch (status) {
			case READY:
				return findAllReady(pageable);
			case AVAILABLE:
				return findAllAvailable(pageable);
			case FINISHED:
				return findAllFinished(pageable);
		}
		return findAllByStatus(status, pageable);
	}

	Page<Exam> findAllByCreator(User creator, Pageable pageable);

	Page<Exam> findAllByCreatorAndStatus(User creator, ExamStatus status, Pageable pageable);

	default Page<Exam> findAllByCreatorAndActualStatus(User creator, ExamStatus status, Pageable pageable) {
		switch (status) {
			case READY:
				return findAllReadyByCreator(creator, pageable);
			case AVAILABLE:
				return findAllAvailableByCreator(creator, pageable);
			case FINISHED:
				return findAllFinishedByCreator(creator, pageable);
		}
		return findAllByCreatorAndStatus(creator, status, pageable);
	}

	default Page<Exam> findAllJoined(int userId, Pageable pageable) {
		List<Exam> examList = findAllJoined(userId, pageable.getOffset(), pageable.getPageSize());
		return new PageImpl<>(examList, pageable, countAllJoined(userId));
	}

	/**
	 * 不能查询到关闭的或结束的并且未参加的考试
	 * @param userId
	 * @param status
	 * @param pageable
	 * @return
	 */
	default Page<Exam> findAllByActualStatus(int userId, ExamStatus status, Pageable pageable){
		switch (status) {
			case READY:
				return findAllReady(pageable);
			case AVAILABLE:
				return findAllAvailable(pageable);
			case FINISHED:
				return findAllJoinedAndFinished(userId, pageable);
			case CLOSED:
				return findAllJoinedAndStatusIs(userId, status, pageable);
		}
		return findAllByStatus(status, pageable);
	}

	default Page<Exam> findAllJoinedAndFinished(int userId, Pageable pageable) {
		LocalDateTime now = LocalDateTime.now();
		List<Exam> examList = findAllJoinedByStatusAndEndAtIsLessThan(userId, ExamStatus.AVAILABLE, now, pageable.getOffset(), pageable.getPageSize());
		return new PageImpl<>(examList, pageable, countAllJoinedByStatusAndEndAtIsLessThan(userId, ExamStatus.AVAILABLE, now));
	}

	default Page<Exam> findAllJoinedAndStatusIs(int userId, ExamStatus status, Pageable pageable) {
		List<Exam> examList = findAllJoinedAndStatusIs(userId, status.toString(), pageable.getOffset(), pageable.getPageSize());
		return new PageImpl<>(examList, pageable, countAllJoinedAndStatusIs(userId, status.toString()));
	}

    /**
     * 查询所有考试，不包括已关闭的
     * @param userId
     * @param pageable
     * @return
     */
    default Page<Exam> findAllExcludeClosed(int userId, Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        List<Exam> examList = findAllJoinedOrStatusAvailable(userId, pageable.getOffset(), pageable.getPageSize());
        return new PageImpl<>(examList, pageable, countAllJoinedOrStatusAvailable(userId));
    }

	@Query(value = "SELECT * FROM ENTITY_EXAM AS e WHERE e.id IN (SELECT r.exam FROM ENTITY_RESULT AS r WHERE r.owner = ?1) ORDER BY e.id DESC LIMIT ?2,?3", nativeQuery = true)
	List<Exam> findAllJoined(int userId, long offset, int limit);

	@Query(value = "SELECT COUNT(*) FROM ENTITY_EXAM AS e WHERE e.id IN (SELECT r.exam FROM ENTITY_RESULT AS r WHERE r.owner = ?1)", nativeQuery = true)
	long countAllJoined(int userId);

	@Query(value = "SELECT * FROM ENTITY_EXAM AS e WHERE e.id IN (SELECT r.exam FROM ENTITY_RESULT AS r WHERE r.owner = ?1) and e.status = ?2 and e.end_at < ?3 ORDER BY e.id DESC LIMIT ?4,?5", nativeQuery = true)
	List<Exam> findAllJoinedByStatusAndEndAtIsLessThan(int userId, ExamStatus status, LocalDateTime now, long offset, int limit);

	@Query(value = "SELECT COUNT(*) FROM ENTITY_EXAM AS e WHERE e.id IN (SELECT r.exam FROM ENTITY_RESULT AS r WHERE r.owner = ?1) and e.status = ?2 and e.end_at < ?3", nativeQuery = true)
	long countAllJoinedByStatusAndEndAtIsLessThan(int userId, ExamStatus status, LocalDateTime now);

	@Query(value = "SELECT * FROM ENTITY_EXAM AS e WHERE e.id IN (SELECT r.exam FROM ENTITY_RESULT AS r WHERE r.owner = ?1) and e.status = ?2 ORDER BY e.id DESC LIMIT ?3,?4", nativeQuery = true)
	List<Exam> findAllJoinedAndStatusIs(int userId, String status, long offset, int limit);

	@Query(value = "SELECT COUNT(*) FROM ENTITY_EXAM AS e WHERE e.id IN (SELECT r.exam FROM ENTITY_RESULT AS r WHERE r.owner = ?1) and e.status = ?2", nativeQuery = true)
	long countAllJoinedAndStatusIs(int userId, String status);

    @Query(value = "SELECT * FROM ENTITY_EXAM AS e WHERE e.id IN (SELECT r.exam FROM ENTITY_RESULT AS r WHERE r.owner = ?1) or e.status = 'AVAILABLE' ORDER BY e.id DESC LIMIT ?2,?3", nativeQuery = true)
    List<Exam> findAllJoinedOrStatusAvailable(int userId, long offset, int limit);

    @Query(value = "SELECT COUNT(*) FROM ENTITY_EXAM AS e WHERE e.id IN (SELECT r.exam FROM ENTITY_RESULT AS r WHERE r.owner = ?1) or e.status = 'AVAILABLE'", nativeQuery = true)
    long countAllJoinedOrStatusAvailable(int userId);

	default Page<Exam> findAllReady(Pageable pageable) {
		return findAllByStatusAndStartAtIsGreaterThan(ExamStatus.AVAILABLE, LocalDateTime.now(), pageable);
	}

	default Page<Exam> findAllAvailable(Pageable pageable) {
		LocalDateTime now = LocalDateTime.now();
		return findAllByStatusAndStartAtIsLessThanAndEndAtIsGreaterThan(ExamStatus.AVAILABLE, now, now, pageable);
	}

	default Page<Exam> findAllFinished(Pageable pageable) {
		return findAllByStatusAndEndAtIsLessThan(ExamStatus.AVAILABLE, LocalDateTime.now(), pageable);
	}

	default Page<Exam> findAllReadyByCreator(User creator, Pageable pageable) {
		return findAllByCreatorAndStatusAndStartAtIsGreaterThan(creator, ExamStatus.AVAILABLE, LocalDateTime.now(), pageable);
	}

	default Page<Exam> findAllAvailableByCreator(User creator, Pageable pageable) {
		LocalDateTime now = LocalDateTime.now();
		return findAllByCreatorAndStatusAndStartAtIsLessThanAndEndAtIsGreaterThan(creator, ExamStatus.AVAILABLE, now, now, pageable);
	}

	default Page<Exam> findAllFinishedByCreator(User creator, Pageable pageable) {
		return findAllByCreatorAndStatusAndEndAtIsLessThan(creator, ExamStatus.AVAILABLE, LocalDateTime.now(), pageable);
	}

	// 不要直接调用下方的方法

	Page<Exam> findAllByStatusAndStartAtIsGreaterThan(ExamStatus status, LocalDateTime startAtGreaterThan, Pageable pageable);

	Page<Exam> findAllByStatusAndStartAtIsLessThanAndEndAtIsGreaterThan(ExamStatus status, LocalDateTime startAtLessThan, LocalDateTime endAtGreaterThan, Pageable pageable);

	Page<Exam> findAllByStatusAndEndAtIsLessThan(ExamStatus status, LocalDateTime endAtLessThan, Pageable pageable);

	Page<Exam> findAllByCreatorAndStatusAndStartAtIsGreaterThan(User creator, ExamStatus status, LocalDateTime startAtGreaterThan, Pageable pageable);

	Page<Exam> findAllByCreatorAndStatusAndStartAtIsLessThanAndEndAtIsGreaterThan(User creator, ExamStatus status, LocalDateTime startAtLessThan, LocalDateTime endAtGreaterThan, Pageable pageable);

	Page<Exam> findAllByCreatorAndStatusAndEndAtIsLessThan(User creator, ExamStatus status, LocalDateTime endAtLessThan, Pageable pageable);
}
