import { Badge } from "@/components/ui/badge";

export function PageHero({ eyebrow, title, subtitle }) {
  return (
    <section className="relative overflow-hidden border-b border-border bg-grid">
      <div className="absolute inset-0 -z-10 bg-gradient-to-b from-secondary/60 to-background" />
      <div className="container py-16 text-center lg:py-20">
        {eyebrow && (
          <Badge variant="accent" className="mb-4">
            {eyebrow}
          </Badge>
        )}
        <h1 className="mx-auto max-w-3xl text-4xl font-extrabold text-balance sm:text-5xl">
          {title}
        </h1>
        {subtitle && (
          <p className="mx-auto mt-5 max-w-2xl text-lg leading-relaxed text-muted-foreground">
            {subtitle}
          </p>
        )}
      </div>
    </section>
  );
}
