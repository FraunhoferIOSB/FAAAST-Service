package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider;

import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import io.adminshell.aas.v3.model.impl.DefaultSubmodelElementCollection;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class InternalSegment extends Segment{

    List<Record> records;

    public InternalSegment(ZonedDateTime start, ZonedDateTime end, List<Record> records) {
        this.records = records;
        this.start = Optional.of(start);
        this.end = Optional.of(end);
    }

    public InternalSegment(List<Record> records) {
        this.records = records;
    }

    public InternalSegment(Record... records) {
        this.records = List.of(records);
    }

    @Override
    public Collection<SubmodelElement> getValues() {
        return records.stream()
                .flatMap(x -> x.getValues().stream())
                .collect(Collectors.toList());
    }
}
