package mekhails.pipeline;

import ru.spbstu.pipeline.RC;

@FunctionalInterface
interface Rule
{
    public RC apply(Object obj);
}
