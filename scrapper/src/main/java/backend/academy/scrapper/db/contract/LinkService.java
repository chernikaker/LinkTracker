package backend.academy.scrapper.db.contract;

import backend.academy.scrapper.entity.Link;
import java.time.LocalDateTime;
import java.util.Map;

/** Контракт сервиса, управляющего ссылками в БД */
public interface LinkService {

    /** Обновление времени последней проверки ссылки */
    void updateLinkValidationOnTime(long linkId, LocalDateTime time);

    /**
     * Получение списка ссылок и их id для проверки
     *
     * @param batchSize размер батча
     * @param offset офсет
     * @param duration максимально допустимое время, которое ссылка не проверяется
     */
    Map<Long, Link> getLinksToCheck(int batchSize, long offset, long duration);
}
