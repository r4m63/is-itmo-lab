package ru.itmo.isitmolab.grid;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import ru.itmo.isitmolab.model.Vehicle;

@ApplicationScoped
public class VehicleService {

    @Inject
    VehicleRepository repo;

    @Transactional
    public Vehicle create(@Valid @NotNull Vehicle v) {
        return repo.save(v);
    }

    @Transactional
    public Vehicle update(Long id, @Valid @NotNull Vehicle patch) {
        Vehicle cur = repo.find(id);
        if (cur == null) throw new IllegalArgumentException("Vehicle not found: " + id);
        // обновляем разрешённые поля (патч)
        cur.setName(patch.getName());
        cur.setType(patch.getType());
        cur.setEnginePower(patch.getEnginePower());
        cur.setDistanceTravelled(patch.getDistanceTravelled());
        cur.setFuelConsumption(patch.getFuelConsumption());
        return repo.save(cur);
    }

    @Transactional
    public void delete(Long id) {
        repo.delete(id);
    }

    public Vehicle get(Long id) {
        return repo.find(id);
    }
}
