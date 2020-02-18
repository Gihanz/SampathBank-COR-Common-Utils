package biz.nable.sb.cor.common.db.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import biz.nable.sb.cor.common.db.entity.CommonTempHis;

@Repository
public interface CommonTempHisRepository extends CrudRepository<CommonTempHis, Long> {

}
