package mekhails.reader;

import ru.spbstu.pipeline.RC;

@FunctionalInterface
interface Rule
{
    public RC apply(Object obj);
}
