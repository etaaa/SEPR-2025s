package at.ac.tuwien.sepr.assignment.individual.mapper;

import at.ac.tuwien.sepr.assignment.individual.dto.OwnerDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Owner;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * Mapper class responsible for converting {@link Owner} entities to {@link OwnerDto} objects.
 */
@Component
public class OwnerMapper {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * Converts an {@link Owner} entity to a corresponding {@link OwnerDto}.
   *
   * @param owner the {@link Owner} entity to convert
   * @return the corresponding {@link OwnerDto}, or null if the input is null
   */
  public OwnerDto entityToDto(Owner owner) {

    LOG.trace("Entering entityToDto [requestId={}]: Converting owner entity {}", MDC.get("r"), owner);

    if (owner == null) {
      LOG.debug("Owner entity is null, returning null [requestId={}]", MDC.get("r"));
      return null;
    }

    OwnerDto result = new OwnerDto(
        owner.id(),
        owner.firstName(),
        owner.lastName(),
        owner.description()
    );

    LOG.debug("Converted owner id {} to OwnerDto [requestId={}]: {}", owner.id(), MDC.get("r"), result);

    return result;
  }
}
