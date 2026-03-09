package uk.co.redsoft.sandbox.domain.ports.out;

import uk.co.redsoft.sandbox.domain.model.CreateBookCommand;

public interface BookImportPort {
    void submitForImport(CreateBookCommand command);
}
