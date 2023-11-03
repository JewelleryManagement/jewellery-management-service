UPDATE resource
SET dtype = 'PreciousStone', clazz = 'PreciousStone'
WHERE clazz = 'Gemstone';

UPDATE resource
SET dtype = 'Metal', clazz = 'Metal'
WHERE clazz = 'PreciousMetal';

UPDATE resource
SET dtype = 'Element', clazz = 'Element'
WHERE clazz = 'LinkingPart';