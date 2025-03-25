package at.ac.tuwien.sepr.assignment.individual.mapper;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailOwnerDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseParentDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Horse;
import at.ac.tuwien.sepr.assignment.individual.exception.FatalException;

import java.lang.invoke.MethodHandles;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * Mapper class responsible for converting {@link Horse} entities into various DTOs.
 */
@Component
public class HorseMapper {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * Converts a {@link Horse} entity into a {@link HorseListDto} for summarized representation.
   *
   * @param horse  the horse entity to convert
   * @param owners a map of owner IDs to {@link HorseDetailOwnerDto} objects, must contain the horse’s owner if referenced
   * @return a {@link HorseListDto} with the horse’s summary details, or null if the input horse is null
   * @throws FatalException if the owners map does not contain the horse’s owner ID when one is referenced
   */
  public HorseListDto entityToListDto(Horse horse, Map<Long, HorseDetailOwnerDto> owners) {

    LOG.trace("Entering entityToListDto [requestId={}]: Converting horse entity {}", MDC.get("r"), horse);

    if (horse == null) {
      LOG.debug("Horse entity is null, returning null [requestId={}]", MDC.get("r"));
      return null;
    }

    HorseListDto result = new HorseListDto(
        horse.id(),
        horse.name(),
        horse.description(),
        horse.dateOfBirth(),
        horse.sex(),
        getOwner(horse, owners)
    );

    LOG.debug("Converted horse id {} to HorseListDto [requestId={}]: {}", horse.id(), MDC.get("r"), result);

    return result;
  }

  /**
   * Converts a {@link Horse} entity into a {@link HorseDetailDto} with detailed information.
   *
   * @param horse  the horse entity to convert
   * @param owners a map of owner IDs to {@link HorseDetailOwnerDto} objects, must contain the horse’s owner if referenced
   * @param mother the {@link HorseParentDto} representing the horse’s mother, or null if none
   * @param father the {@link HorseParentDto} representing the horse’s father, or null if none
   * @return a {@link HorseDetailDto} with detailed horse data, or null if the input horse is null
   * @throws FatalException if the owners map does not contain the horse’s owner ID when one is referenced
   */
  public HorseDetailDto entityToDetailDto(Horse horse, Map<Long, HorseDetailOwnerDto> owners, HorseParentDto mother, HorseParentDto father) {

    LOG.trace("Entering entityToDetailDto [requestId={}]: Converting horse entity {}", MDC.get("r"), horse);

    if (horse == null) {
      LOG.debug("Horse entity is null, returning null [requestId={}]", MDC.get("r"));

      return null;
    }

    HorseDetailDto result = new HorseDetailDto(
        horse.id(),
        horse.name(),
        horse.description(),
        horse.dateOfBirth(),
        horse.sex(),
        getOwner(horse, owners),
        mother,
        father,
        horse.imageUrl()
    );

    LOG.debug("Converted horse id {} to HorseDetailDto [requestId={}]: {}", horse.id(), MDC.get("r"), result);

    return result;
  }

  /**
   * Retrieves the owner DTO for a horse from the provided owners map.
   *
   * @param horse  the horse entity whose owner is to be retrieved
   * @param owners a map of owner IDs to {@link HorseDetailOwnerDto} objects, must contain the horse’s owner if referenced
   * @return the {@link HorseDetailOwnerDto} for the horse’s owner, or null if no owner is referenced
   * @throws FatalException if the owners map does not contain the horse’s owner ID when one is referenced
   */
  private HorseDetailOwnerDto getOwner(Horse horse, Map<Long, HorseDetailOwnerDto> owners) {

    LOG.trace("Retrieving owner for horse id {} [requestId={}]: ownerId {}",
        horse.id(), MDC.get("r"), horse.ownerId());

    HorseDetailOwnerDto owner = null;
    var ownerId = horse.ownerId();
    if (ownerId != null) {
      if (!owners.containsKey(ownerId)) {
        LOG.error("Unexpected error [requestId={}]: Owner map missing owner ID {} for horse id {}",
            MDC.get("r"), ownerId, horse.id());

        throw new FatalException("Given owner map does not contain owner of this Horse (%d)".formatted(horse.id()));
      }
      owner = owners.get(ownerId);

      LOG.debug("Retrieved owner for horse id {} [requestId={}]: ownerId {}", horse.id(), MDC.get("r"), ownerId);
    }

    return owner;
  }
}
